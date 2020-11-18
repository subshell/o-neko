package io.oneko.projectmesh.rest;

import io.oneko.kubernetes.deployments.DeploymentDTO;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.templates.rest.ConfigurationTemplateDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@Data
public class MeshComponentDTO {
	private UUID id;
	private String name;
	private UUID projectId;
	private UUID projectVersionId;
	private Map<String, String> templateVariables;
	private List<ConfigurationTemplateDTO> configurationTemplates = new ArrayList<>();
	private boolean outdated;
	private List<String> urls;
	private DeploymentDTO deployment;
	private DesiredState desiredState;
}
