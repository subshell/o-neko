package io.oneko.kubernetes.deployments.persistence;

import io.oneko.kubernetes.deployments.DeployableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
@Builder
public class DeploymentMongo {

	@Id
	private UUID id;

	@Indexed(unique = true)
	@Field("projectVersionId")
	private UUID deployableId;

	private DeployableStatus status;
	private Instant timestamp;
	@Builder.Default
	private int containerCount = 0;
	@Builder.Default
	private int readyContainerCount = 0;
}
