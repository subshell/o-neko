package io.oneko.kubernetes.impl;

import static io.oneko.util.MoreStructuredArguments.*;
import static net.logstash.logback.argument.StructuredArguments.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.oneko.event.CurrentEventTrigger;
import io.oneko.event.EventTrigger;
import io.oneko.event.ScheduledTask;
import io.oneko.helm.HelmCommands;
import io.oneko.helm.HelmRegistryException;
import io.oneko.helmapi.model.Status;
import io.oneko.kubernetes.deployments.DeployableStatus;
import io.oneko.kubernetes.deployments.Deployment;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.kubernetes.deployments.ReadableDeployment;
import io.oneko.kubernetes.deployments.WritableDeployment;
import io.oneko.metrics.MetricNameBuilder;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.project.ProjectVersionLock;
import io.oneko.project.ReadableProject;
import io.oneko.project.WritableProjectVersion;
import io.oneko.websocket.SessionWebSocketHandler;
import io.oneko.websocket.message.DeploymentStatusChangedMessage;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
class DeploymentStatusWatcher {

	private final ProjectRepository projectRepository;
	private final DeploymentRepository deploymentRepository;
	private final SessionWebSocketHandler webSocketHandler;
	private final HelmStatusToDeploymentMapper helmStatusToDeploymentMapper;
	private final CurrentEventTrigger currentEventTrigger;
	private final HelmCommands helmCommands;
	private final ProjectVersionLock projectVersionLock;

	private final Timer checkDeploymentStatusTimer;

	DeploymentStatusWatcher(ProjectRepository projectRepository,
													DeploymentRepository deploymentRepository,
													SessionWebSocketHandler webSocketHandler,
													HelmStatusToDeploymentMapper helmStatusToDeploymentMapper,
													CurrentEventTrigger currentEventTrigger,
													HelmCommands helmCommands,
													ProjectVersionLock projectVersionLock,
													MeterRegistry meterRegistry) {
		this.projectRepository = projectRepository;
		this.deploymentRepository = deploymentRepository;
		this.webSocketHandler = webSocketHandler;
		this.helmStatusToDeploymentMapper = helmStatusToDeploymentMapper;
		this.currentEventTrigger = currentEventTrigger;
		this.helmCommands = helmCommands;
		this.projectVersionLock = projectVersionLock;
		this.checkDeploymentStatusTimer = Timer.builder(new MetricNameBuilder().durationOf("kubernetes.deployment.status.check").build())
				.description("the time it takes to iterate over all relevant deployments and update their status")
				.publishPercentileHistogram()
				.minimumExpectedValue(Duration.ofMillis(500))
				.register(meterRegistry);
	}


	private EventTrigger asTrigger() {
		return new ScheduledTask("Kubernetes Deployment Status Watcher");
	}

	@Scheduled(fixedRate = 10_000)
	protected void updateProjectStatus() {
		checkDeploymentStatusTimer.record(() -> {
			final List<WritableProjectVersion> writableVersions = projectRepository.getAll().stream()
					.map(ReadableProject::writable)
					.flatMap(writableProject -> writableProject.getVersions().stream())
					.filter(this::shouldScanDeployable)
					.collect(Collectors.toList());

			try (var ignored = currentEventTrigger.forTryBlock(asTrigger())) {
				writableVersions.forEach(this::scanResourcesForDeployable);
			}
		});
	}

	private boolean shouldScanDeployable(ProjectVersion<?, ?> projectVersion) {
		return projectVersion.getDesiredState() == DesiredState.Deployed
				|| deploymentRepository.findByProjectVersionId(projectVersion.getId()).isPresent();
	}

	private void scanResourcesForDeployable(ProjectVersion<?, ?> projectVersion) {
		projectVersionLock.doWithProjectVersionLock(projectVersion, () -> {
			try {
				final List<Status> statuses = helmCommands.status(projectVersion);

				if (statuses.isEmpty()) {
					cleanUpOnDeploymentRemoved(projectVersion);
					return;
				}

				final WritableDeployment deployment = getOrCreateDeploymentForVersion(projectVersion);
				ensureDeploymentIsUpToDate(deployment, statuses, projectVersion);

			} catch (HelmRegistryException e) {
				log.error("failed to get helm status ({}, {})", versionKv(projectVersion), projectKv(projectVersion.getProject()));
			}
		});
	}

	private WritableDeployment getOrCreateDeploymentForVersion(ProjectVersion<?, ?> projectVersion) {
		return deploymentRepository.findByProjectVersionId(projectVersion.getId())
				.map(ReadableDeployment::writable)
				.orElseGet(() -> WritableDeployment.getDefaultDeployment(projectVersion.getId()));
	}

	private void cleanUpOnDeploymentRemoved(ProjectVersion<?, ?> projectVersion) {
		deploymentRepository.findByProjectVersionId(projectVersion.getId())
				.ifPresent(deployment -> {
					log.debug("updating status {} => {} ({}, {})", v("from", deployment.getStatus()), v("to", "Deleted"), versionKv(projectVersion), projectKv(projectVersion.getProject()));
					deploymentRepository.deleteById(deployment.getId());
					this.dispatchWebsocketEventFor(projectVersion, null);
				});
	}

	private void ensureDeploymentIsUpToDate(WritableDeployment deployment, List<Status> statuses, ProjectVersion<?, ?> projectVersion) {
		DeployableStatus previousStatus = deployment.getStatus();
		this.helmStatusToDeploymentMapper.updateDeploymentFromHelmReleaseStatus(deployment, statuses);

		if (!deployment.isDirty()) {
			return;
		}

		final ReadableDeployment savedDeployment = deploymentRepository.save(deployment);

		if (previousStatus != savedDeployment.getStatus()) {
			log.debug("updating status {} => {} ({}, {})", v("from", previousStatus), v("to", deployment.getStatus()), versionKv(projectVersion), projectKv(projectVersion.getProject()));
		}
		this.dispatchWebsocketEventFor(projectVersion, savedDeployment);
	}

	private void dispatchWebsocketEventFor(ProjectVersion<?, ?> deployable, Deployment deployment) {
		DeployableStatus newStatus = deployment != null ? deployment.getStatus() : DeployableStatus.NotScheduled;
		Instant mysteriousRefDate = deployment != null ? deployment.getTimestamp().orElse(null) : null;
		log.trace("dispatching websocket event with new status ({}, {})", versionKv(deployable), kv("new_status", newStatus));
		webSocketHandler.broadcast(new DeploymentStatusChangedMessage(deployable.getId(), deployable.getProject().getId(), newStatus, deployable.getDesiredState(), mysteriousRefDate, deployable.isOutdated(), deployable.getImageUpdatedDate()));
	}
}
