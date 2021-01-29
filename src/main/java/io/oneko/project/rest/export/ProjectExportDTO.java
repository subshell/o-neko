package io.oneko.project.rest.export;

import java.util.List;
import java.util.UUID;

import io.oneko.automations.LifetimeBehaviourDTO;
import io.oneko.deployable.AggregatedDeploymentStatus;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.project.rest.TemplateVariableDTO;
import io.oneko.templates.rest.ConfigurationTemplateDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ProjectExportDTO {
	private String name;
	private String imageName;
	private DeploymentBehaviour newVersionsDeploymentBehaviour;
	private List<ConfigurationTemplateDTO> defaultConfigurationTemplates;
	private List<TemplateVariableDTO> templateVariables;
	private UUID dockerRegistryUUID;
	private AggregatedDeploymentStatus status;
	private LifetimeBehaviourDTO defaultLifetimeBehaviour;

	private ProjectExportMetadataDTO exportMetadata;
}
