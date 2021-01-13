package io.oneko.project.persistence;

import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.kubernetes.deployments.DesiredState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class ProjectVersionMongo {
	@Id
	private UUID projectVersionUuid;
	private String name;
	private DeploymentBehaviour deploymentBehaviour;
	private String dockerContentDigest;
	private List<String> urls;
	private List<ConfigurationTemplateMongo> configurationTemplates;
	private boolean outdated;
	private LifetimeBehaviour lifetimeBehaviour;
	private UUID namespace;
	private Map<String, String> templateVariables;
	private DesiredState desiredState;
	private Instant imageUpdatedDate;
}
