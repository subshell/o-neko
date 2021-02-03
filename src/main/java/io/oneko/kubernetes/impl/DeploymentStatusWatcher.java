package io.oneko.kubernetes.impl;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.oneko.event.CurrentEventTrigger;
import io.oneko.event.EventTrigger;
import io.oneko.event.ScheduledTask;
import io.oneko.helm.HelmRegistryException;
import io.oneko.helm.util.HelmCommandUtils;
import io.oneko.helmapi.model.Status;
import io.oneko.kubernetes.deployments.DeployableStatus;
import io.oneko.kubernetes.deployments.Deployment;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.kubernetes.deployments.ReadableDeployment;
import io.oneko.kubernetes.deployments.WritableDeployment;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
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

	DeploymentStatusWatcher(ProjectRepository projectRepository,
													DeploymentRepository deploymentRepository,
													SessionWebSocketHandler webSocketHandler,
													HelmStatusToDeploymentMapper helmStatusToDeploymentMapper, CurrentEventTrigger currentEventTrigger) {
		this.projectRepository = projectRepository;
		this.deploymentRepository = deploymentRepository;
		this.webSocketHandler = webSocketHandler;
		this.helmStatusToDeploymentMapper = helmStatusToDeploymentMapper;
		this.currentEventTrigger = currentEventTrigger;
	}

	private EventTrigger asTrigger() {
		return new ScheduledTask("Kubernetes Deployment Status Watcher");
	}

	@Scheduled(fixedRate = 5000)
	protected void updateProjectStatus() {
		final List<WritableProjectVersion> writableVersions = projectRepository.getAll().stream()
				.map(ReadableProject::writable)
				.flatMap(writableProject -> writableProject.getVersions().stream())
				.filter(this::shouldScanDeployable)
				.collect(Collectors.toList());

		try (var ignored = currentEventTrigger.forTryBlock(asTrigger())) {
			writableVersions.forEach(this::scanResourcesForDeployable);
		}
	}

	private boolean shouldScanDeployable(ProjectVersion<?, ?> projectVersion) {
		return projectVersion.getDesiredState() == DesiredState.Deployed
				|| deploymentRepository.findByProjectVersionId(projectVersion.getId()).isPresent();
	}

	private void scanResourcesForDeployable(ProjectVersion<?, ?> projectVersion) {
		try {
			final List<Status> statuses = HelmCommandUtils.status(projectVersion);

			if (statuses.isEmpty()) {
				cleanUpOnDeploymentRemoved(projectVersion);
				return;
			}

			final WritableDeployment deployment = getOrCreateDeploymentForVersion(projectVersion);
			ensureDeploymentIsUpToDate(deployment, statuses, projectVersion);

		} catch (HelmRegistryException e) {
			log.error("Failed to get helm status for {}", projectVersion.getNameWithProjectName());
		}
	}

	private WritableDeployment getOrCreateDeploymentForVersion(ProjectVersion<?, ?> projectVersion) {
		return deploymentRepository.findByProjectVersionId(projectVersion.getId())
				.map(ReadableDeployment::writable)
				.orElseGet(() -> WritableDeployment.getDefaultDeployment(projectVersion.getId()));
	}

	private void cleanUpOnDeploymentRemoved(ProjectVersion<?, ?> projectVersion) {
		deploymentRepository.findByProjectVersionId(projectVersion.getId())
				.ifPresent(deployment -> {
					log.debug("Updating status of {} from {} to deleted", projectVersion.getName(), deployment.getStatus());
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
			log.debug("Updating status of {} from {} to {}", projectVersion.getNameWithProjectName(), previousStatus, deployment.getStatus());
		}
		this.dispatchWebsocketEventFor(projectVersion, savedDeployment);
	}

	private void dispatchWebsocketEventFor(ProjectVersion<?, ?> deployable, Deployment deployment) {
		DeployableStatus newStatus = deployment != null ? deployment.getStatus() : DeployableStatus.NotScheduled;
		Instant mysteriousRefDate = deployment != null ? deployment.getTimestamp().orElse(null) : null;
		log.trace("Dispatching Websocket event for deployable {} with new status {}", deployable.getName(), newStatus);
		webSocketHandler.broadcast(new DeploymentStatusChangedMessage(deployable.getId(), deployable.getProject().getId(), newStatus, deployable.getDesiredState(), mysteriousRefDate, deployable.isOutdated(), deployable.getImageUpdatedDate()));
	}
}
