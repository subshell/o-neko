package io.oneko.automations;

import static io.oneko.util.MoreStructuredArguments.*;
import static net.logstash.logback.argument.StructuredArguments.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.oneko.kubernetes.DeploymentManager;
import io.oneko.kubernetes.deployments.DeployableStatus;
import io.oneko.kubernetes.deployments.Deployment;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import io.oneko.metrics.MetricNameBuilder;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.project.ReadableProject;
import io.oneko.project.WritableProjectVersion;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ScheduledLifetimeController {

	private final LifetimeBehaviourService lifetimeBehaviourService;
	private final ProjectRepository projectRepository;
	private final DeploymentRepository deploymentRepository;
	private final DeploymentManager deploymentManager;

	private final Timer scheduledProjectCheckTimer;
	private final Timer expiredDeploymentStopTimer;
	private final Timer retrieveExpiredDeploymentsTimer;

	public ScheduledLifetimeController(LifetimeBehaviourService lifetimeBehaviourService,
																		 ProjectRepository projectRepository,
																		 DeploymentRepository deploymentRepository,
																		 DeploymentManager deploymentManager,
																		 MeterRegistry meterRegistry) {
		this.lifetimeBehaviourService = lifetimeBehaviourService;
		this.projectRepository = projectRepository;
		this.deploymentRepository = deploymentRepository;
		this.deploymentManager = deploymentManager;

		this.scheduledProjectCheckTimer = Timer.builder(new MetricNameBuilder().durationOf("lifetime.scheduled.checkProjects").build())
				.description("the time it takes O-Neko to check all projects for versions which have a lifetime configuration which needs to be checked")
				.publishPercentileHistogram()
				.register(meterRegistry);
		this.retrieveExpiredDeploymentsTimer = Timer.builder(new MetricNameBuilder().durationOf("lifetime.scheduled.deployments.retrieveExpired").build())
				.description("the time it takes O-Neko to filter and retrieve expired deployments")
				.publishPercentileHistogram()
				.register(meterRegistry);
		this.expiredDeploymentStopTimer = Timer.builder(new MetricNameBuilder().durationOf("lifetime.scheduled.deployments.stopExpired").build())
				.description("the time it takes O-Neko to stop an individual expired deployment")
				.publishPercentileHistogram()
				.register(meterRegistry);
	}

	@Scheduled(fixedRate = 5 * 60000)
	public void checkProjects() {
		final var sample = Timer.start();
		final List<ProjectVersion<?, ?>> versions = projectRepository.getAll().stream()
				.map(ReadableProject::writable)
				.flatMap(project -> project.getVersions().stream())
				.filter(this::shouldConsiderVersion)
				.collect(Collectors.toList());
		sample.stop(scheduledProjectCheckTimer);
		stopExpiredDeployments(versions,
				projectVersion -> log.info("deployment expired ({}, {})", versionKv(projectVersion), projectKv(projectVersion.getProject())));
	}

	private void stopExpiredDeployments(List<ProjectVersion<?, ?>> deployables, Consumer<ProjectVersion<?, ?>> beforeStopDeployment) {
		final Timer.Sample retrieveDeploymentsStart = Timer.start();
		final var deployments = getRelevantDeploymentsFor(deployables);
		final var expiredPairsOfDeployableAndDeployment = getExpiredPairsOfDeployableAndDeployment(deployables, deployments);
		retrieveDeploymentsStart.stop(retrieveExpiredDeploymentsTimer);

		expiredPairsOfDeployableAndDeployment.forEach(expiredDeploymentStopTimer.record(() ->
				expiredVersionDeploymentPair -> {
					final var projectVersion = expiredVersionDeploymentPair.getLeft();
					beforeStopDeployment.accept(projectVersion);
					if (projectVersion instanceof WritableProjectVersion) {
						deploymentManager.stopDeploymentAsync((WritableProjectVersion) projectVersion);
					} else {
						log.error("stopping is not supported ({})", kv("class_name", projectVersion.getClass()));
					}
				})
		);
	}

	private boolean shouldConsiderVersion(ProjectVersion<?, ?> version) {
		final var effectiveLifetimeBehaviour = version.getEffectiveLifetimeBehaviour();
		return shouldConsider(effectiveLifetimeBehaviour);
	}

	private boolean shouldConsider(Optional<LifetimeBehaviour> behaviour) {
		return behaviour.isPresent() && !behaviour.get().isInfinite();
	}

	private List<Deployment> getRelevantDeploymentsFor(List<ProjectVersion<?, ?>> deployables) {
		final var uuids = deployables.stream().map(ProjectVersion::getId).collect(Collectors.toSet());
		return deploymentRepository.findAllByProjectVersionIdIn(uuids).stream()
				.filter(deployment -> !deployment.getStatus().equals(DeployableStatus.NotScheduled))
				.collect(Collectors.toList());
	}

	private Set<Pair<ProjectVersion<?, ?>, Deployment>> getExpiredPairsOfDeployableAndDeployment(List<ProjectVersion<?, ?>> versions, List<Deployment> deployments) {
		var combiningFunction = createExpiredDeployableDeploymentCombiningFunction(versions);
		return deployments.stream()
				.map(combiningFunction)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	//what a method name
	private Function<Deployment, Optional<Pair<ProjectVersion<?, ?>, Deployment>>> createExpiredDeployableDeploymentCombiningFunction(List<ProjectVersion<?, ?>> deployables) {
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
