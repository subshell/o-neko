package io.oneko.kubernetes.impl;

import io.fabric8.kubernetes.api.model.Pod;
import io.oneko.event.CurrentEventTrigger;
import io.oneko.event.EventTrigger;
import io.oneko.event.ScheduledTask;
import io.oneko.kubernetes.deployments.*;
import io.oneko.namespace.Namespace;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.project.ReadableProject;
import io.oneko.project.WritableProjectVersion;
import io.oneko.projectmesh.MeshService;
import io.oneko.projectmesh.ProjectMeshRepository;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableMeshComponent;
import io.oneko.websocket.SessionWebSocketHandler;
import io.oneko.websocket.message.DeploymentStatusChangedMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
class DeploymentStatusWatcher {

	private final KubernetesAccess kubernetesAccess;
	private final ProjectRepository projectRepository;
	private final ProjectMeshRepository meshRepository;
	private final DeploymentRepository deploymentRepository;
	private final SessionWebSocketHandler webSocketHandler;
	private final PodToDeploymentMapper podToDeploymentMapper;
	private final CurrentEventTrigger currentEventTrigger;
	private final MeshService meshService;

	DeploymentStatusWatcher(KubernetesAccess kubernetesAccess, ProjectRepository projectRepository,
							ProjectMeshRepository meshRepository, DeploymentRepository deploymentRepository,
							SessionWebSocketHandler webSocketHandler,
							PodToDeploymentMapper podToDeploymentMapper, CurrentEventTrigger currentEventTrigger, MeshService meshService) {
		this.kubernetesAccess = kubernetesAccess;
		this.projectRepository = projectRepository;
		this.meshRepository = meshRepository;
		this.deploymentRepository = deploymentRepository;
		this.webSocketHandler = webSocketHandler;
		this.podToDeploymentMapper = podToDeploymentMapper;
		this.currentEventTrigger = currentEventTrigger;
		this.meshService = meshService;
	}

	private EventTrigger asTrigger() {
		return new ScheduledTask("Kubernetes Deployment Status Watcher");
	}

	@Scheduled(fixedRate = 5000)
	protected void updateProjectStatus() {
		final List<WritableProjectVersion> writableVersions = projectRepository.getAll().stream()
				.map(ReadableProject::writable)
				.flatMap(writableProject -> writableProject.getVersions().stream())
				.filter(writableVersion -> shouldScanDeployable(Deployables.of(writableVersion)))
				.collect(Collectors.toList());

		try (var ignored = currentEventTrigger.forTryBlock(asTrigger())) {
			writableVersions.forEach(version -> scanResourcesForDeployable(version.getNamespace(), Deployables.of(version)));
		}
	}

	@Scheduled(fixedRate = 5000, initialDelay = 2500)
	protected void updateMeshStatus() {
		final List<WritableMeshComponent> writableMeshComponents = meshRepository.getAll().stream()
				.map(ReadableProjectMesh::writable)
				.flatMap(writableProjectMesh -> writableProjectMesh.getComponents().stream())
				.filter(meshComponent -> shouldScanDeployable(Deployables.of(meshComponent, meshService)))
				.collect(Collectors.toList());

		try (var ignored = currentEventTrigger.forTryBlock(asTrigger())) {
			writableMeshComponents
					.forEach(component -> scanResourcesForDeployable(component.getOwner().getNamespace(), Deployables.of(component, meshService)));
		}
	}

	private boolean shouldScanDeployable(Deployable<?> deployable) {
		return deployable.getDesiredState() == DesiredState.Deployed
				|| deploymentRepository.findByDeployableId(deployable.getId()).isPresent();
	}

	private void scanResourcesForDeployable(Namespace namespace, Deployable<?> deployable) {
		final List<Pod> podsByLabelInNameSpace = kubernetesAccess.getPodsByLabelInNameSpace(namespace.asKubernetesNameSpace(), deployable.getPrimaryLabel());
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
		} else if (deployable.getEntity() instanceof WritableMeshComponent) {
			WritableMeshComponent meshComponent = (WritableMeshComponent) deployable.getEntity();
			webSocketHandler.broadcast(new DeploymentStatusChangedMessage(deployable.getId(), meshComponent.getOwner().getId(), DeploymentStatusChangedMessage.DeployableType.meshComponent, newStatus, deployable.getDesiredState(), mysteriousRefDate, deployable.isOutdated(), deployable.getRelatedProjectVersion().getImageUpdatedDate()));
		}
	}
}
