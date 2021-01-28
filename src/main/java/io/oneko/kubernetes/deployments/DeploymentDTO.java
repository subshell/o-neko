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

	public static DeploymentDTO create(UUID deployableId, Deployment deployment) {
		if (deployment == null) {
			deployment = WritableDeployment.getDefaultDeployment(deployableId);
		}
		return DeploymentDTO.builder()
				.status(deployment.getStatus())
				.timestamp(deployment.getTimestamp().orElse(null))
				.build();
	}
}
