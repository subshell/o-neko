package io.oneko.kubernetes.deployments;

import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStateWaiting;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.PodStatus;

import java.util.List;
import java.util.stream.Collectors;

public enum DeployableStatus {
	Pending,
	Running,
	Failed,
	Unknown,
	NotScheduled;

	public static DeployableStatus fromPodStatus(PodStatus podStatus) {
		DeployableStatus status = Unknown;
		try {
			status = DeployableStatus.valueOf(podStatus.getPhase());
			if (!podStatus.getContainerStatuses().isEmpty()) {
				List<DeployableStatus> containerStatuses = podStatus.getContainerStatuses()
						.stream()
						.map(DeployableStatus::fromContainerStatus)
						.collect(Collectors.toList());
				if (containerStatuses.contains(Failed)) {
					status = Failed;
				} else if (containerStatuses.contains(Pending)) {
					status = Pending;
				}
			}
		} catch (IllegalArgumentException e) {
			// ok
		}
		return status;
	}

	public static DeployableStatus fromContainerStatus(ContainerStatus containerStatus) {
		ContainerState state = containerStatus.getState();
		if (state.getRunning() != null && containerStatus.getReady()) {
			return Running;
		} else if (state.getRunning() != null && !containerStatus.getReady()) {
			return Pending;
		} else if (state.getWaiting() != null) {
			final ContainerStateWaiting waiting = state.getWaiting();
			if (waiting.getReason().equals("ContainerCreating")) {
				return Pending;
			} else if (waiting.getReason().equals("ErrImagePull") || waiting.getReason().equals("ImagePullBackOff") || waiting.getReason().equals("CrashLoopBackOff")) {
				return Failed;
			}
		}
		return Unknown;
	}
}
