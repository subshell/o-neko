package io.oneko.kubernetes.deployments;

import io.oneko.deployable.AggregatedDeploymentStatus;
import lombok.experimental.UtilityClass;

import java.util.Collection;

@UtilityClass
public class DeploymentDTOs {

	public static AggregatedDeploymentStatus aggregate(Collection<DeploymentDTO> deploymentDTOs) {
		if (deploymentDTOs.isEmpty()) {
			return AggregatedDeploymentStatus.NotDeployed;
		}
		boolean hasUnscheduled = false;
		boolean hasFailures = false;
		boolean hasPendings = false;
		for (DeploymentDTO deploymentDTO : deploymentDTOs) {
			if (deploymentDTO == null) {
				continue;
			}
			if (deploymentDTO.getStatus() == DeployableStatus.NotScheduled) {
				hasUnscheduled = true;
			}
			if (deploymentDTO.getStatus() == DeployableStatus.Failed) {
				hasFailures = true;
			}
			if (deploymentDTO.getStatus() == DeployableStatus.Pending) {
				hasPendings = true;
			}
		}
		//aggregation is like: order by severness
		if (hasFailures) {
			return AggregatedDeploymentStatus.Error;
		}
		if (hasPendings) {
			return AggregatedDeploymentStatus.Pending;
		}
		if (hasUnscheduled) {
			return AggregatedDeploymentStatus.NotDeployed;
		}
		return AggregatedDeploymentStatus.Ok;
	}
}
