package io.oneko.kubernetes.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.oneko.kubernetes.deployments.DeployableStatus;
import io.oneko.kubernetes.deployments.WritableDeployment;

@Component
public class PodToDeploymentMapper {

	public WritableDeployment updateDeploymentFromPods(WritableDeployment deployment, List<Pod> pods) {
		List<PodStatus> podStatuses = pods.stream()
				.map(Pod::getStatus)
				.collect(Collectors.toList());

		Set<DeployableStatus> deployableStatuses = pods.stream()
				.map(pod -> DeployableStatus.fromPodStatus(pod.getStatus()))
				.collect(Collectors.toSet());

		setStatus(deployment, deployableStatuses);
		setTimestamp(deployment, podStatuses);

		return deployment;
	}

	private void setTimestamp(WritableDeployment deployment, List<PodStatus> podStatuses) {
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

	private void setStatus(WritableDeployment deployment, Set<DeployableStatus> deployableStatuses) {
		if (deployableStatuses.isEmpty() && deployment.getStatus() != DeployableStatus.NotScheduled) {
			deployment.setStatus(DeployableStatus.NotScheduled);
		} else if (deployableStatuses.contains(DeployableStatus.Failed) && deployment.getStatus() != DeployableStatus.Failed) {
			deployment.setStatus(DeployableStatus.Failed);
		} else if (deployableStatuses.contains(DeployableStatus.Pending) && deployment.getStatus() != DeployableStatus.Pending) {
			deployment.setStatus(DeployableStatus.Pending);
		} else if (deployableStatuses.contains(DeployableStatus.Running) && deployment.getStatus() != DeployableStatus.Running) {
			deployment.setStatus(DeployableStatus.Running);
		} else if (deployableStatuses.contains(DeployableStatus.Unknown) && deployment.getStatus() != DeployableStatus.Unknown) {
			deployment.setStatus(DeployableStatus.Unknown);
		}
	}

}
