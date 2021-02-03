package io.oneko.kubernetes.deployments;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeploymentDTO {
	private DeployableStatus status;
	private Instant timestamp;

	public static DeploymentDTO create(UUID projectVersionId, Deployment deployment) {
		if (deployment == null) {
			deployment = WritableDeployment.getDefaultDeployment(projectVersionId);
		}
		return DeploymentDTO.builder()
				.status(deployment.getStatus())
				.timestamp(deployment.getTimestamp().orElse(null))
				.build();
	}
}
