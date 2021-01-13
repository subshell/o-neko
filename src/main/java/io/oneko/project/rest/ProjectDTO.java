package io.oneko.project.rest;

import io.oneko.automations.LifetimeBehaviourDTO;
import io.oneko.deployable.AggregatedDeploymentStatus;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.templates.rest.ConfigurationTemplateDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Data
public class ProjectDTO {
	private UUID uuid;
	private String name;
	private String imageName;
	private DeploymentBehaviour newVersionsDeploymentBehaviour;
	private List<ConfigurationTemplateDTO> defaultConfigurationTemplates = new ArrayList<>();
	private List<TemplateVariableDTO> templateVariables;
	private UUID dockerRegistryUUID;
	private List<ProjectVersionDTO> versions = new ArrayList<>();
	private AggregatedDeploymentStatus status;
	private LifetimeBehaviourDTO defaultLifetimeBehaviour;
}
