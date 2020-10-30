package io.oneko.automations;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.oneko.kubernetes.KubernetesDeploymentManager;
import io.oneko.kubernetes.deployments.Deployable;
import io.oneko.kubernetes.deployments.DeployableStatus;
import io.oneko.kubernetes.deployments.Deployables;
import io.oneko.kubernetes.deployments.Deployment;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import io.oneko.project.ReadableProject;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.project.WritableProject;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableProjectMesh;
import io.oneko.projectmesh.ProjectMeshRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Component
@Slf4j
public class ScheduledLifetimeController {

	private final ProjectRepository projectRepository;
	private final ProjectMeshRepository meshRepository;
	private final DeploymentRepository deploymentRepository;
	private final KubernetesDeploymentManager kubernetesDeploymentManager;

	public ScheduledLifetimeController(ProjectRepository projectRepository, ProjectMeshRepository meshRepository, DeploymentRepository deploymentRepository, KubernetesDeploymentManager kubernetesDeploymentManager) {
		this.projectRepository = projectRepository;
		this.meshRepository = meshRepository;
		this.deploymentRepository = deploymentRepository;
		this.kubernetesDeploymentManager = kubernetesDeploymentManager;
	}

	@Scheduled(fixedRate = 5 * 60000)
	public void checkProjects() {
		projectRepository.getAll()
				.map(ReadableProject::writable)
				.flatMapIterable(WritableProject::getVersions)
				.filter(this::shouldConsiderVersion)
				.map(Deployables::of)
				.collectList()
				.zipWhen(this::getRelevantDeploymentsFor)
				.flux()
				.flatMapIterable(this::getExpiredPairsOfDeployableAndDeployment)
				.flatMap(expiredVersionDeploymentPair -> {
					final var deployable = expiredVersionDeploymentPair.getLeft();
					log.info("Deployment of project version {} of project {} expired.", deployable.getRelatedProjectVersion().getName(), deployable.getRelatedProject().getName());
					return kubernetesDeploymentManager.stopDeployment(deployable.getEntity());
				})
				.doOnError(e -> log.error(e.getMessage(), e))
				.subscribe();
	}

	@Scheduled(fixedRate = 5 * 60000, initialDelay = 2 * 60000)
	public void checkProjectVersions() {
		meshRepository.getAll()
				.map(ReadableProjectMesh::writable)
				.flatMapIterable(WritableProjectMesh::getComponents)
				.filter(component -> this.shouldConsider(component.getOwner().getLifetimeBehaviour()))
				.map(Deployables::of)
				.collectList()
				.zipWhen(this::getRelevantDeploymentsFor)
				.flux()
				.flatMapIterable(this::getExpiredPairsOfDeployableAndDeployment)
				.flatMap(expiredVersionDeploymentPair -> {
					final var deployable = expiredVersionDeploymentPair.getLeft();
					log.info("Deployment of component {}  expired.", deployable.getFullLabel());
					return kubernetesDeploymentManager.stopDeployment(deployable.getEntity());
				}).subscribe(any -> {
		}, e -> log.error(e.getMessage(), e));
	}

	private boolean shouldConsiderVersion(ProjectVersion<?, ?> version) {
		final var effectiveLifetimeBehaviour = version.getEffectiveLifetimeBehaviour();
		return shouldConsider(effectiveLifetimeBehaviour);
	}

	private boolean shouldConsider(Optional<LifetimeBehaviour> behaviour) {
		return behaviour.isPresent() && !behaviour.get().isInfinite();
	}

	private Mono<List<Deployment>> getRelevantDeploymentsFor(List<? extends Deployable> deployables) {
		final var uuids = deployables.stream().map(Deployable::getId).collect(Collectors.toSet());
		return deploymentRepository.findAllByDeployableIdIn(uuids).filter(deployment -> !deployment.getStatus().equals(DeployableStatus.NotScheduled)).collectList();
	}

	private <T extends Deployable> Set<Pair<T, Deployment>> getExpiredPairsOfDeployableAndDeployment(Tuple2<List<T>, List<Deployment>> tuple) {
		var versions = tuple.getT1();
		var deployments = tuple.getT2();
		var combiningFunction = createExpiredDeployableDeploymentCombiningFunction(versions);
		return deployments.stream()
				.map(combiningFunction)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	//what a method name
	private <T extends Deployable> Function<Deployment, Optional<Pair<T, Deployment>>> createExpiredDeployableDeploymentCombiningFunction(List<T> deployables) {
		return (deployment) -> {

			final var matchingDeployableOptional = deployables.stream()
					.filter(d -> d.getId().equals(deployment.getDeployableId()))
					.findFirst();

			if (matchingDeployableOptional.isPresent()) {
				final var matchingDeployable = matchingDeployableOptional.get();
				final Optional<LifetimeBehaviour> effectiveLifetimeBehaviour = matchingDeployable.calculateEffectiveLifetimeBehaviour();
				if (effectiveLifetimeBehaviour.isPresent()) {
					if (effectiveLifetimeBehaviour.get().isExpired(deployment)) {
						return Optional.of(Pair.of(matchingDeployable, deployment));
					}
				}
			}

			return Optional.empty();
		};
	}

}
