package io.oneko.projectmesh;

import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.project.ProjectConstants;
import io.oneko.templates.ConfigurationTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MeshComponent<M extends ProjectMesh<M, C>, C extends MeshComponent<M, C>> {

	UUID getId();

	String getName();

	M getOwner();

	UUID getProjectId();

	UUID getProjectVersionId();

	String getDockerContentDigest();

	List<String> getUrls();

	boolean isOutdated();

	/**
	 * Provides a mutable copy of the template variables explicitly set on this version.
	 */
	Map<String, String> getTemplateVariables();

	default Map<String, String> getImplicitTemplateVariables() {
		Map<String, String> implicitTemplateVariables = new HashMap<>();
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.MESH_NAME, getOwner().getName());
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.ONEKO_MESH, getOwner().getId().toString());
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.MESH_COMPONENT_NAME, getName());
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.ONEKO_MESH_COMPONENT, getOwner().getId().toString());
		return implicitTemplateVariables;
	}

	List<? extends ConfigurationTemplate> getConfigurationTemplates();

	DesiredState getDesiredState();

}
