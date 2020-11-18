package io.oneko.projectmesh;

import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.project.ProjectConstants;
import io.oneko.project.ReadableProject;
import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.TemplateVariable;
import io.oneko.templates.ConfigurationTemplate;
import io.oneko.templates.ConfigurationTemplates;
import io.oneko.templates.WritableConfigurationTemplate;
import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public interface MeshComponent<M extends ProjectMesh<M, C>, C extends MeshComponent<M, C>> {

	UUID getId();

	String getName();

	M getOwner();

	ReadableProject getProject();

	ReadableProjectVersion getProjectVersion();

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

	/**
	 * Provides a mutable copy of all template variables retrieved by merging the ones from this version's
	 * {@link #getProject()} and this version's own {@link #getTemplateVariables()}.
	 *
	 * @return Never <code>null</code>
	 */
	default Map<String, String> calculateEffectiveTemplateVariables() {
		Map<String, String> mergedTemplateVariables = new HashMap<>();
		mergedTemplateVariables.putAll(getProjectVersion().getImplicitTemplateVariables());
		mergedTemplateVariables.putAll(getProject().getTemplateVariables().stream()
				.collect(Collectors.toMap(TemplateVariable::getName, TemplateVariable::getDefaultValue)));
		mergedTemplateVariables.putAll(getProjectVersion().getTemplateVariables());
		mergedTemplateVariables.putAll(getImplicitTemplateVariables());
		mergedTemplateVariables.putAll(getTemplateVariables());
		return mergedTemplateVariables;
	}

	List<? extends ConfigurationTemplate> getConfigurationTemplates();

	/**
	 * Provides all effective templates to use on this component. This is either derived from the project's configuration
	 * template, a modified version template or a modified template straight from this component with the effective
	 * template variables filled in.
	 */
	default List<WritableConfigurationTemplate> getCalculatedConfigurationTemplates() {
		StringSubstitutor sub = new StringSubstitutor(this.calculateEffectiveTemplateVariables());
		return ConfigurationTemplates.unifyTemplateSets(getProject().getDefaultConfigurationTemplates(), getProjectVersion().getConfigurationTemplates(), getConfigurationTemplates())
				.stream()
				.map(WritableConfigurationTemplate::clone)
				.peek(template -> template.setContent(sub.replace(template.getContent())))
				.collect(Collectors.toList());
	}

	DesiredState getDesiredState();

}
