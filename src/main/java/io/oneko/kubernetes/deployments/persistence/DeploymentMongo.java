package io.oneko.kubernetes.deployments.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import io.oneko.kubernetes.deployments.DeployableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
@Builder
public class DeploymentMongo {

	@Id
	private UUID id;

	@Indexed(unique = true)
	private UUID projectVersionId;

	private DeployableStatus status;
	private Instant timestamp;

}
