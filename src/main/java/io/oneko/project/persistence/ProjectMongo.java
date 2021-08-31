package io.oneko.project.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
class ProjectMongo {
	@Id
	private UUID projectUuid;
	@Indexed(unique = true)
	private String name;
	private String imageName;
	private DeploymentBehaviour newVersionsDeploymentBehaviour;
	private List<String> urlTemplates;
	private List<ConfigurationTemplateMongo> defaultConfigurationTemplates;
	@Indexed
	private UUID dockerRegistryUUID;
	private List<ProjectVersionMongo> versions;
	private LifetimeBehaviour defaultLifetimeBehaviour;
	private List<TemplateVariableMongo> templateVariables;
	private String namespace;
}
