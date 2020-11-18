package io.oneko.kubernetes.deployments;

import io.oneko.deployable.DeploymentBehaviour;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeploymentDTO {
	private DeployableStatus status;
	private Instant timestamp;
	@Builder.Default
	private int containerCount = 0;
	@Builder.Default
	private int readyContainerCount = 0;

	public static DeploymentDTO create(DeploymentBehaviour behaviour, UUID deployableId, Deployment deployment) {
		if (deployment == null) {
			deployment = Deployment.getDefaultDeployment(behaviour, deployableId);
		}
		return DeploymentDTO.builder()
				.containerCount(deployment.getContainerCount())
				.readyContainerCount(deployment.getReadyContainerCount())
				.status(deployment.getStatus())
				.timestamp(deployment.getTimestamp().orElse(null))
				.build();
	}
}
