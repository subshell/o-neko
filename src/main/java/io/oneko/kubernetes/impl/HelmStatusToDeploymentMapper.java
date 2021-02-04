package io.oneko.kubernetes.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.oneko.helmapi.model.Status;
import io.oneko.kubernetes.deployments.DeployableStatus;
import io.oneko.kubernetes.deployments.WritableDeployment;

@Component
public class HelmStatusToDeploymentMapper {

	public WritableDeployment updateDeploymentFromHelmReleaseStatus(WritableDeployment deployment, List<Status> statuses) {
		final var deployableStatuses = statuses.stream().map(status -> status.getInfo().getStatus())
				.map(DeployableStatus::fromReleaseStatus).collect(Collectors.toSet());
		final var releaseNames = statuses.stream().map(Status::getName).collect(Collectors.toList());

		setTimestamp(deployment, statuses);
		setStatus(deployment, deployableStatuses);
		deployment.setReleaseNames(releaseNames);
		return deployment;
	}

	private void setTimestamp(WritableDeployment deployment, List<Status> statuses) {
		Optional<Instant> timestamp = deployment.getTimestamp();

		Instant newTimestamp = statuses.stream()
				.map(status -> status.getInfo().getLastDeployed().toInstant())
				.sorted()
				.findFirst()
				.orElse(null);

		if (timestamp.isEmpty() || newTimestamp != null && newTimestamp.isAfter(timestamp.get())) {
			deployment.setTimestamp(newTimestamp);
		} else if (timestamp.isPresent()) {
			deployment.setTimestamp(timestamp.get());
		}
	}

	private void setStatus(WritableDeployment deployment, Collection<DeployableStatus> deployableStatuses) {
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
