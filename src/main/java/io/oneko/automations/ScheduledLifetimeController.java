package io.oneko.automations;

import io.oneko.kubernetes.KubernetesDeploymentManager;
import io.oneko.kubernetes.deployments.*;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.project.ReadableProject;
import io.oneko.project.WritableProjectVersion;
import io.oneko.projectmesh.MeshService;
import io.oneko.projectmesh.ProjectMeshRepository;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableMeshComponent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ScheduledLifetimeController {

	private final ProjectRepository projectRepository;
	private final ProjectMeshRepository meshRepository;
	private final DeploymentRepository deploymentRepository;
	private final KubernetesDeploymentManager kubernetesDeploymentManager;
	private final MeshService meshService;

	public ScheduledLifetimeController(ProjectRepository projectRepository, ProjectMeshRepository meshRepository, DeploymentRepository deploymentRepository, KubernetesDeploymentManager kubernetesDeploymentManager, MeshService meshService) {
		this.projectRepository = projectRepository;
		this.meshRepository = meshRepository;
		this.deploymentRepository = deploymentRepository;
		this.kubernetesDeploymentManager = kubernetesDeploymentManager;
		this.meshService = meshService;
	}

	@Scheduled(fixedRate = 5 * 60000)
	public void checkProjects() {
		final var versions = projectRepository.getAll().stream()
				.map(ReadableProject::writable)
				.flatMap(project -> project.getVersions().stream())
				.filter(this::shouldConsiderVersion)
				.map(Deployables::of)
				.collect(Collectors.toList());

		stopExpiredDeployments(versions,
				deployable -> log.info("Deployment of project version {} of project {} expired.", deployable.getRelatedProjectVersion().getName(), deployable.getRelatedProject().getName()));
	}

	@Scheduled(fixedRate = 5 * 60000, initialDelay = 2 * 60000)
	public void checkProjectVersions() {
		final var meshComponents = meshRepository.getAll().stream()
				.map(ReadableProjectMesh::writable)
				.flatMap(mesh -> mesh.getComponents().stream())
				.filter(component -> this.shouldConsider(component.getOwner().getLifetimeBehaviour()))
				.map(component -> Deployables.of(component, meshService))
				.collect(Collectors.toList());

		stopExpiredDeployments(meshComponents, deployable -> log.info("Deployment of component {} expired.", deployable.getFullLabel()));
	}

	private <T> void stopExpiredDeployments(List<Deployable<T>> deployables, Consumer<Deployable<T>> beforeStopDeployment) {
		final var deployments = getRelevantDeploymentsFor(deployables);
		final var expiredPairsOfDeployableAndDeployment = getExpiredPairsOfDeployableAndDeployment(deployables, deployments);

		expiredPairsOfDeployableAndDeployment.forEach(expiredVersionDeploymentPair -> {
			final var deployable = expiredVersionDeploymentPair.getLeft();
			beforeStopDeployment.accept(deployable);
			if (deployable instanceof WritableMeshComponent) {
				kubernetesDeploymentManager.stopDeployment((WritableMeshComponent) deployable.getEntity());
			} else if (deployable instanceof WritableProjectVersion) {
				kubernetesDeploymentManager.stopDeployment((WritableProjectVersion) deployable.getEntity());
			} else {
				log.error("Stopping {} is not supported.", deployable.getClass());
			}
		});
	}

	private boolean shouldConsiderVersion(ProjectVersion<?, ?> version) {
		final var effectiveLifetimeBehaviour = version.getEffectiveLifetimeBehaviour();
		return shouldConsider(effectiveLifetimeBehaviour);
	}

	private boolean shouldConsider(Optional<LifetimeBehaviour> behaviour) {
		return behaviour.isPresent() && !behaviour.get().isInfinite();
	}

	private List<Deployment> getRelevantDeploymentsFor(List<? extends Deployable<?>> deployables) {
		final var uuids = deployables.stream().map(Deployable::getId).collect(Collectors.toSet());
		return deploymentRepository.findAllByDeployableIdIn(uuids).stream()
				.filter(deployment -> !deployment.getStatus().equals(DeployableStatus.NotScheduled))
				.collect(Collectors.toList());
	}

	private <T extends Deployable<?>> Set<Pair<T, Deployment>> getExpiredPairsOfDeployableAndDeployment(List<T> versions, List<Deployment> deployments) {
		var combiningFunction = createExpiredDeployableDeploymentCombiningFunction(versions);
		return deployments.stream()
				.map(combiningFunction)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	//what a method name
	private <T extends Deployable<?>> Function<Deployment, Optional<Pair<T, Deployment>>> createExpiredDeployableDeploymentCombiningFunction(List<T> deployables) {
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
