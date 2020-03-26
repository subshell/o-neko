package io.oneko.kubernetes.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Pod;
import io.oneko.event.EventTrigger;
import io.oneko.event.ScheduledTask;
import io.oneko.kubernetes.deployments.Deployable;
import io.oneko.kubernetes.deployments.DeployableStatus;
import io.oneko.kubernetes.deployments.Deployables;
import io.oneko.kubernetes.deployments.Deployment;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.namespace.Namespace;
import io.oneko.project.ReadableProject;
import io.oneko.project.WritableProject;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableMeshComponent;
import io.oneko.projectmesh.WritableProjectMesh;
import io.oneko.projectmesh.ProjectMeshRepository;
import io.oneko.websocket.ReactiveWebSocketHandler;
import io.oneko.websocket.message.DeploymentStatusChangedMessage;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
@Slf4j
class DeploymentStatusWatcher {

	private final KubernetesAccess kubernetesAccess;
	private final ProjectRepository projectRepository;
	private final ProjectMeshRepository meshRepository;
	private final DeploymentRepository deploymentRepository;
	private final ReactiveWebSocketHandler webSocketHandler;
	private final PodToDeploymentMapper podToDeploymentMapper;

	DeploymentStatusWatcher(KubernetesAccess kubernetesAccess, ProjectRepository projectRepository,
							ProjectMeshRepository meshRepository, DeploymentRepository deploymentRepository,
							ReactiveWebSocketHandler webSocketHandler,
							PodToDeploymentMapper podToDeploymentMapper) {
		this.kubernetesAccess = kubernetesAccess;
		this.projectRepository = projectRepository;
		this.meshRepository = meshRepository;
		this.deploymentRepository = deploymentRepository;
		this.webSocketHandler = webSocketHandler;
		this.podToDeploymentMapper = podToDeploymentMapper;
	}

	@Scheduled(fixedRate = 5000)
	protected void updateProjectStatus() {
		projectRepository.getAll()
				.map(ReadableProject::writable)
				.flatMapIterable(WritableProject::getVersions)
				.filterWhen(projectVersion -> shouldScanDeployable(Deployables.of(projectVersion)))
				.flatMap(version -> scanResourcesForDeployable(version.getNamespace(), Deployables.of(version)))
				.subscriberContext(Context.of(EventTrigger.class, new ScheduledTask("Kubernetes Status Watcher")))
				.subscribe(null, e -> log.error(e.getMessage(), e));
	}

	@Scheduled(fixedRate = 5000, initialDelay = 2500)
	protected void updateMeshStatus() {
		meshRepository.getAll()
				.map(ReadableProjectMesh::writable)
				.flatMapIterable(WritableProjectMesh::getComponents)
				.filterWhen(meshComponent -> shouldScanDeployable(Deployables.of(meshComponent)))
				.flatMap(component -> scanResourcesForDeployable(component.getOwner().getNamespace(), Deployables.of(component)))
				.subscriberContext(Context.of(EventTrigger.class, new ScheduledTask("Kubernetes Status Watcher")))
				.subscribe(null, e -> log.error(e.getMessage(), e));
	}

	private Mono<Boolean> shouldScanDeployable(Deployable deployable) {
		if (deployable.getDesiredState() == DesiredState.Deployed) {
			return Mono.just(true);
		} else {
			return deploymentRepository.findByDeployableId(deployable.getId()).hasElement();
		}
	}

	private Mono<?> scanResourcesForDeployable(Namespace namespace, Deployable<?> deployable) {
		final List<Pod> podsByLabelInNameSpace = kubernetesAccess.getPodsByLabelInNameSpace(namespace.asKubernetesNameSpace(), deployable.getPrimaryLabel());
		if (!podsByLabelInNameSpace.isEmpty()) {
			return Mono.just(podsByLabelInNameSpace)
					.zipWith(this.getOrCreateDeploymentForDeployable(deployable))
					.flatMap(tuple -> this.ensureDeploymentIsUpToDate(tuple.getT2(), tuple.getT1(), deployable));
		} else {
			return cleanUpOnDeploymentRemoved(deployable);
		}
	}

	private Mono<Deployment> getOrCreateDeploymentForDeployable(Deployable deployable) {
		return deploymentRepository.findByDeployableId(deployable.getId())
				.defaultIfEmpty(Deployment.getDefaultDeployment(deployable.getDeploymentBehaviour(), deployable.getId()));
	}

	private Mono<Void> cleanUpOnDeploymentRemoved(Deployable<?> deployable) {
		return deploymentRepository.findByDeployableId(deployable.getId())
				.flatMap(deployment -> {
					log.debug("Updating status of {} from {} to deleted", deployable.getFullLabel(), deployment.getStatus());
					return deploymentRepository.deleteById(deployment.getId()).doOnSuccess(ignore -> this.dispatchWebsocketEventFor(deployable, null));
				});
	}

	private Mono<Deployment> ensureDeploymentIsUpToDate(Deployment deployment, List<Pod> pods, Deployable deployable) {
		DeployableStatus previousStatus = deployment.getStatus();
		this.podToDeploymentMapper.updateDeploymentFromPods(deployment, pods);

		if (deployment.isDirty()) {
			return deploymentRepository.save(deployment)
					.doOnNext(d -> {
						if (previousStatus != deployment.getStatus()) {
							log.debug("Updating status of {} from {} to {}", deployable.getFullLabel(), previousStatus, deployment.getStatus());
						}
						this.dispatchWebsocketEventFor(deployable, d);
					});
		} else {
			return Mono.just(deployment);
		}
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
			webSocketHandler.broadcast(new DeploymentStatusChangedMessage(deployable.getId(), meshComponent.getOwner().getId(), DeploymentStatusChangedMessage.DeployableType.meshComponent, newStatus, deployable.getDesiredState(), mysteriousRefDate, deployable.isOutdated(), meshComponent.getProjectVersion().getImageUpdatedDate()));
		}
	}

}
