package io.oneko.kubernetes.impl;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Pod;
import io.oneko.event.CurrentEventTrigger;
import io.oneko.event.EventTrigger;
import io.oneko.event.ScheduledTask;
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

	private final KubernetesAccess kubernetesAccess;
	private final ProjectRepository projectRepository;
	private final DeploymentRepository deploymentRepository;
	private final SessionWebSocketHandler webSocketHandler;
	private final PodToDeploymentMapper podToDeploymentMapper;
	private final CurrentEventTrigger currentEventTrigger;

	DeploymentStatusWatcher(KubernetesAccess kubernetesAccess, ProjectRepository projectRepository,
							DeploymentRepository deploymentRepository,
							SessionWebSocketHandler webSocketHandler,
							PodToDeploymentMapper podToDeploymentMapper, CurrentEventTrigger currentEventTrigger) {
		this.kubernetesAccess = kubernetesAccess;
		this.projectRepository = projectRepository;
		this.deploymentRepository = deploymentRepository;
		this.webSocketHandler = webSocketHandler;
		this.podToDeploymentMapper = podToDeploymentMapper;
		this.currentEventTrigger = currentEventTrigger;
	}

	private EventTrigger asTrigger() {
		return new ScheduledTask("Kubernetes Deployment Status Watcher");
	}
	/*

	@Scheduled(fixedRate = 5000)
	protected void updateProjectStatus() {
		final List<WritableProjectVersion> writableVersions = projectRepository.getAll().stream()
				.map(ReadableProject::writable)
				.flatMap(writableProject -> writableProject.getVersions().stream())
				.filter(this::shouldScanDeployable)
				.collect(Collectors.toList());

		try (var ignored = currentEventTrigger.forTryBlock(asTrigger())) {
			writableVersions.forEach(version -> scanResourcesForDeployable(version.getNamespace(), version));
		}
	}

	private boolean shouldScanDeployable(ProjectVersion<?,?> deployable) {
		return deployable.getDesiredState() == DesiredState.Deployed
				|| deploymentRepository.findByDeployableId(deployable.getId()).isPresent();
	}

	private void scanResourcesForDeployable(String namespace, ProjectVersion<?,?> deployable) {
		final List<Pod> podsByLabelInNameSpace = kubernetesAccess.getPodsByLabelInNameSpace(namespace, deployable.get());
		if (podsByLabelInNameSpace.isEmpty()) {
			cleanUpOnDeploymentRemoved(deployable);
			return;
		}

		final WritableDeployment deployment = getOrCreateDeploymentForDeployable(deployable);
		podsByLabelInNameSpace.forEach(pod -> this.ensureDeploymentIsUpToDate(deployment, podsByLabelInNameSpace, deployable));
	}

	private WritableDeployment getOrCreateDeploymentForDeployable(Deployable<?> deployable) {
		return deploymentRepository.findByDeployableId(deployable.getId())
				.map(ReadableDeployment::writable)
				.orElseGet(() -> WritableDeployment.getDefaultDeployment(deployable.getId()));
	}

	private void cleanUpOnDeploymentRemoved(Deployable<?> deployable) {
		deploymentRepository.findByDeployableId(deployable.getId())
				.ifPresent(deployment -> {
					log.debug("Updating status of {} from {} to deleted", deployable.getFullLabel(), deployment.getStatus());
					deploymentRepository.deleteById(deployment.getId());
					this.dispatchWebsocketEventFor(deployable, null);
				});
	}

	private void ensureDeploymentIsUpToDate(WritableDeployment deployment, List<Pod> pods, Deployable<?> deployable) {
		DeployableStatus previousStatus = deployment.getStatus();
		this.podToDeploymentMapper.updateDeploymentFromPods(deployment, pods);

		if (!deployment.isDirty()) {
			return;
		}

		final ReadableDeployment savedDeployment = deploymentRepository.save(deployment);

		if (previousStatus != savedDeployment.getStatus()) {
			log.debug("Updating status of {} from {} to {}", deployable.getFullLabel(), previousStatus, deployment.getStatus());
		}
		this.dispatchWebsocketEventFor(deployable, savedDeployment);
	}

	private void dispatchWebsocketEventFor(Deployable<?> deployable, Deployment deployment) {
		DeployableStatus newStatus = deployment != null ? deployment.getStatus() : DeployableStatus.NotScheduled;
		Instant mysteriousRefDate = deployment != null ? deployment.getTimestamp().orElse(null) : null;
		log.trace("Dispatching Websocket event for deployable {} with new status {}", deployable.getName(), newStatus);
		if (deployable.getEntity() instanceof ProjectVersion) {
			ProjectVersion version = (ProjectVersion) deployable.getEntity();
			webSocketHandler.broadcast(new DeploymentStatusChangedMessage(deployable.getId(), version.getProject().getId(), DeploymentStatusChangedMessage.DeployableType.projectVersion, newStatus, deployable.getDesiredState(), mysteriousRefDate, deployable.isOutdated(), version.getImageUpdatedDate()));
		}
	}

	 */
}
