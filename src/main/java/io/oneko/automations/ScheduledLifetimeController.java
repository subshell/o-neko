package io.oneko.automations;

import static io.oneko.util.MoreStructuredArguments.projectKv;
import static io.oneko.util.MoreStructuredArguments.versionKv;
import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.oneko.kubernetes.DeploymentManager;
import io.oneko.kubernetes.deployments.DeployableStatus;
import io.oneko.kubernetes.deployments.Deployment;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.project.ReadableProject;
import io.oneko.project.WritableProjectVersion;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@AllArgsConstructor
public class ScheduledLifetimeController {

	private final LifetimeBehaviourService lifetimeBehaviourService;
	private final ProjectRepository projectRepository;
	private final DeploymentRepository deploymentRepository;
	private final DeploymentManager deploymentManager;

	@Scheduled(fixedRate = 5 * 60000)
	public void checkProjects() {
		final List<ProjectVersion<?,?>> versions = projectRepository.getAll().stream()
				.map(ReadableProject::writable)
				.flatMap(project -> project.getVersions().stream())
				.filter(this::shouldConsiderVersion)
				.collect(Collectors.toList());

		stopExpiredDeployments(versions,
				projectVersion -> log.info("deployment expired ({}, {})", versionKv(projectVersion), projectKv(projectVersion.getProject())));
	}

	private void stopExpiredDeployments(List<ProjectVersion<?,?>> deployables, Consumer<ProjectVersion<?,?>> beforeStopDeployment) {
		final var deployments = getRelevantDeploymentsFor(deployables);
		final var expiredPairsOfDeployableAndDeployment = getExpiredPairsOfDeployableAndDeployment(deployables, deployments);

		expiredPairsOfDeployableAndDeployment.forEach(expiredVersionDeploymentPair -> {
			final var projectVersion = expiredVersionDeploymentPair.getLeft();
			beforeStopDeployment.accept(projectVersion);
			if (projectVersion instanceof WritableProjectVersion) {
				deploymentManager.stopDeployment((WritableProjectVersion) projectVersion);
			} else {
				log.error("stopping is not supported ({})", kv("class_name", projectVersion.getClass()));
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

	private List<Deployment> getRelevantDeploymentsFor(List<ProjectVersion<?,?>> deployables) {
		final var uuids = deployables.stream().map(ProjectVersion::getId).collect(Collectors.toSet());
		return deploymentRepository.findAllByProjectVersionIdIn(uuids).stream()
				.filter(deployment -> !deployment.getStatus().equals(DeployableStatus.NotScheduled))
				.collect(Collectors.toList());
	}

	private Set<Pair<ProjectVersion<?,?>, Deployment>> getExpiredPairsOfDeployableAndDeployment(List<ProjectVersion<?,?>> versions, List<Deployment> deployments) {
		var combiningFunction = createExpiredDeployableDeploymentCombiningFunction(versions);
		return deployments.stream()
				.map(combiningFunction)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	//what a method name
	private Function<Deployment, Optional<Pair<ProjectVersion<?,?>, Deployment>>> createExpiredDeployableDeploymentCombiningFunction(List<ProjectVersion<?,?>> deployables) {
		return (deployment) -> {

			final var matchingDeployableOptional = deployables.stream()
					.filter(d -> d.getId().equals(deployment.getProjectVersionId()))
					.findFirst();

			if (matchingDeployableOptional.isPresent()) {
				final var matchingDeployable = matchingDeployableOptional.get();
				final Optional<LifetimeBehaviour> effectiveLifetimeBehaviour = matchingDeployable.getEffectiveLifetimeBehaviour();
				if (effectiveLifetimeBehaviour.isPresent()) {
					if (lifetimeBehaviourService.isExpired(effectiveLifetimeBehaviour.get(), deployment)) {
						return Optional.of(Pair.of(matchingDeployable, deployment));
					}
				}
			}

			return Optional.empty();
		};
	}

}
