package io.oneko.kubernetes.impl;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.oneko.kubernetes.deployments.DeployableStatus;
import io.oneko.kubernetes.deployments.Deployment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PodToDeploymentMapper {

	public Deployment updateDeploymentFromPods(Deployment deployment, List<Pod> pods) {
		List<PodStatus> podStatuses = pods.stream()
				.map(Pod::getStatus)
				.collect(Collectors.toList());

		Set<DeployableStatus> deployableStatuses = pods.stream()
				.map(pod -> DeployableStatus.fromPodStatus(pod.getStatus()))
				.collect(Collectors.toSet());

		setStatus(deployment, deployableStatuses);
		setContainerCount(deployment, podStatuses);
		setReadyCount(deployment, podStatuses);
		setTimestamp(deployment, podStatuses);

		return deployment;
	}

	private void setTimestamp(Deployment deployment, List<PodStatus> podStatuses) {
		Optional<Instant> timestamp = deployment.getTimestamp();

		Instant newTimestamp = podStatuses.stream()
				.map(podStatus -> Instant.parse(podStatus.getStartTime()))
				.sorted()
				.findFirst()
				.orElse(null);

		if (timestamp.isEmpty() || newTimestamp != null && newTimestamp.isAfter(timestamp.get())) {
			deployment.setTimestamp(newTimestamp);
		} else if (timestamp.isPresent()) {
			deployment.setTimestamp(timestamp.get());
		}
	}

	private void setReadyCount(Deployment deployment, List<PodStatus> podStatuses) {
		int newReadyCount = (int) podStatuses.stream()
				.flatMap(ps -> ps.getContainerStatuses().stream())
				.map(ContainerStatus::getReady)
				.filter(ready -> ready)
				.count();
		deployment.setReadyContainerCount(newReadyCount);
	}

	private void setContainerCount(Deployment deployment, List<PodStatus> podStatuses) {
		int newContainerCount = (int) podStatuses.stream()
				.map(ps -> ps.getContainerStatuses().size())
				.count();

		deployment.setContainerCount(newContainerCount);
	}

	private void setStatus(Deployment deployment, Set<DeployableStatus> deployableStatuses) {
		if (deployableStatuses.isEmpty() && deployment.getStatus() != DeployableStatus.NotScheduled) {
			deployment.setStatus(DeployableStatus.NotScheduled);
		} else if (deployableStatuses.contains(DeployableStatus.Failed) && deployment.getStatus() != DeployableStatus.Failed) {
			deployment.setStatus(DeployableStatus.Failed);
		} else if (deployableStatuses.contains(DeployableStatus.Pending) && deployment.getStatus() != DeployableStatus.Pending) {
			deployment.setStatus(DeployableStatus.Pending);
		} else if (deployableStatuses.contains(DeployableStatus.Running) && deployment.getStatus() != DeployableStatus.Running) {
			deployment.setStatus(DeployableStatus.Running);
		} else if (deployableStatuses.contains(DeployableStatus.Succeeded) && deployment.getStatus() != DeployableStatus.Succeeded) {
			deployment.setStatus(DeployableStatus.Succeeded);
		} else if (deployableStatuses.contains(DeployableStatus.Unknown) && deployment.getStatus() != DeployableStatus.Unknown) {
			deployment.setStatus(DeployableStatus.Unknown);
		}
	}

}
