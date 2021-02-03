package io.oneko.kubernetes.deployments;

import java.util.Arrays;

import io.oneko.helmapi.model.ReleaseStatus;

public enum DeployableStatus {
	Pending,
	Running,
	Failed,
	Unknown,
	NotScheduled;

	public static DeployableStatus fromReleaseStatus(ReleaseStatus releaseStatus) {
		DeployableStatus status = Unknown;
		if (releaseStatus == ReleaseStatus.deployed) {
			status = Running;
		} else if (Arrays.asList(ReleaseStatus.pendingInstall, ReleaseStatus.pendingUpgrade, ReleaseStatus.pendingRollback, ReleaseStatus.uninstalled, ReleaseStatus.superseded).contains(releaseStatus)) {
			status = Pending;
		} else if (releaseStatus == ReleaseStatus.failed) {
			status = Failed;
		}
		return status;
	}
}
