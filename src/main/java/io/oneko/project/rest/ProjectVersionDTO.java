package io.oneko.project.rest;

import io.oneko.automations.LifetimeBehaviourDTO;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.kubernetes.deployments.DeploymentDTO;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.namespace.rest.NamespaceDTO;
import io.oneko.templates.rest.ConfigurationTemplateDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@Data
public class ProjectVersionDTO {

	private UUID uuid;
	private String name;
	private DeploymentBehaviour deploymentBehaviour;
	private List<TemplateVariableDTO> availableTemplateVariables;
	private Map<String, String> templateVariables;
	private DeploymentDTO deployment;
	private List<String> urls;
	private List<ConfigurationTemplateDTO> configurationTemplates;
	private boolean outdated;
	private LifetimeBehaviourDTO lifetimeBehaviour;
	private NamespaceDTO implicitNamespace;
	private NamespaceDTO namespace;
	private DesiredState desiredState;
	private Instant imageUpdatedDate;
}
