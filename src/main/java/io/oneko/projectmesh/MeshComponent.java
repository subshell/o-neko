package io.oneko.projectmesh;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.text.StringSubstitutor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareListProperty;
import io.oneko.domain.ModificationAwareMapProperty;
import io.oneko.domain.ModificationAwareProperty;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.project.Project;
import io.oneko.project.ProjectConstants;
import io.oneko.project.ProjectVersion;
import io.oneko.project.TemplateVariable;
import io.oneko.templates.ConfigurationTemplate;
import io.oneko.templates.ConfigurationTemplates;
import lombok.Builder;
import lombok.Getter;

public class MeshComponent extends ModificationAwareIdentifiable {

	@Getter
	private final ProjectMesh owner;
	private final ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
	private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private final Project project;
	private final ModificationAwareProperty<ProjectVersion> projectVersion = new ModificationAwareProperty<>(this, "projectVersion");
	private final ModificationAwareProperty<String> dockerContentDigest = new ModificationAwareProperty<>(this, "dockerContentDigest");
	private final ModificationAwareProperty<Map<String, String>> templateVariables = new ModificationAwareMapProperty<>(this, "templateVariables");
	private final ModificationAwareProperty<List<ConfigurationTemplate>> configurationTemplates = new ModificationAwareListProperty<>(this, "configurationTemplates");
	private final ModificationAwareProperty<Boolean> outdated = new ModificationAwareProperty<>(this, "outdated");
	private final ModificationAwareProperty<List<String>> urls = new ModificationAwareListProperty<>(this, "urls");
	private final ModificationAwareProperty<DesiredState> desiredState = new ModificationAwareProperty<>(this, "desiredState");

	@Builder
	public MeshComponent(ProjectMesh owner, UUID id, String name, Project project, ProjectVersion version, String dockerContentDigest, Map<String, String> templateVariables, List<ConfigurationTemplate> configurationTemplates, boolean outdated, List<String> urls, DesiredState desiredState) {
		this.owner = owner;
		this.id.init(id);
		this.name.init(name);
		this.project = project;
		this.projectVersion.init(version);
		this.dockerContentDigest.init(dockerContentDigest);
		this.templateVariables.init(templateVariables);
		this.configurationTemplates.init(configurationTemplates);
		this.outdated.init(outdated);
		this.urls.init(urls);
		this.desiredState.init(Objects.requireNonNullElse(desiredState, DesiredState.NotDeployed));
	}

	/**
	 * Creates a mesh of the given project.
	 */
	public MeshComponent(ProjectMesh owner, Project project, ProjectVersion version) {
		this.owner = Objects.requireNonNull(owner);
		this.id.set(UUID.randomUUID());
		this.project = Objects.requireNonNull(project);
		this.projectVersion.set(Objects.requireNonNull(version));
		this.outdated.set(false);
		this.desiredState.set(DesiredState.NotDeployed);
	}

	@Override
	public UUID getId() {
		return this.id.get();
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public Project getProject() {
		return project;
	}

	public ProjectVersion getProjectVersion() {
		return this.projectVersion.get();
	}

	public void setProjectVersion(ProjectVersion version) {
		Preconditions.checkArgument(Objects.equals(version.getProject(), this.project), "The Version of a mesh component can just be changed to another version of the project it is already assigned to.");
		this.projectVersion.set(version);
	}

	public String getDockerContentDigest() {
		return dockerContentDigest.get();
	}

	public void setDockerContentDigest(String dockerContentDigest) {
		this.dockerContentDigest.set(dockerContentDigest);
	}

	public List<String> getUrls() {
		return urls.get();
	}

	public void setUrls(List<String> urls) {
		this.urls.set(urls);
	}

	public boolean isOutdated() {
		return this.outdated.get();
	}

	public void setOutdated(boolean outdated) {
		this.outdated.set(outdated);
	}

	/**
	 * Provides a mutable copy of the template variables explicitly set on this version.
	 */
	public Map<String, String> getTemplateVariables() {
		return this.templateVariables.get();
	}

	public void setTemplateVariables(Map<String, String> templateVariables) {
		this.templateVariables.set(templateVariables);
	}

	private Map<String, String> getImplicitTemplateVariables() {
		Map<String, String> implicitTemplateVariables = new HashMap<>();
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.MESH_NAME, this.owner.getName());
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.ONEKO_MESH, this.owner.getId().toString());
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.MESH_COMPONENT_NAME, this.getName());
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.ONEKO_MESH_COMPONENT, this.owner.getId().toString());
		return implicitTemplateVariables;
	}

	/**
	 * Provides a mutable copy of all template variables retrieved by merging the ones from this version's
	 * {@link #getProject()} and this version's own {@link #getTemplateVariables()}.
	 *
	 * @return Never <code>null</code>
	 */
	public Map<String, String> calculateEffectiveTemplateVariables() {
		Map<String, String> mergedTemplateVariables = new HashMap<>();
		mergedTemplateVariables.putAll(this.getProjectVersion().getImplicitTemplateVariables());
		mergedTemplateVariables.putAll(this.getProject().getTemplateVariables().stream()
				.collect(Collectors.toMap(TemplateVariable::getName, TemplateVariable::getDefaultValue)));
		mergedTemplateVariables.putAll(this.getProjectVersion().getTemplateVariables());
		mergedTemplateVariables.putAll(this.getImplicitTemplateVariables());
		mergedTemplateVariables.putAll(this.getTemplateVariables());
		return mergedTemplateVariables;
	}

	public List<ConfigurationTemplate> getConfigurationTemplates() {
		return this.configurationTemplates.get();
	}

	public void setConfigurationTemplates(List<ConfigurationTemplate> configurationTemplates) {
		ConfigurationTemplates.ensureConsistentCollection(configurationTemplates);
		this.configurationTemplates.set(configurationTemplates);
	}

	/**
	 * Provides all effective templates to use on this component. This is either derived from the project's configuration
	 * template, a modified version template or a modified template straight from this component with the effective
	 * template variables filled in.
	 */
	public List<ConfigurationTemplate> getCalculatedConfigurationTemplates() {
		StringSubstitutor sub = new StringSubstitutor(this.calculateEffectiveTemplateVariables());
		return ConfigurationTemplates.unifyTemplateSets(project.getDefaultConfigurationTemplates(), getProjectVersion().getConfigurationTemplates(), getConfigurationTemplates())
				.stream()
				.map(ConfigurationTemplate::clone)
				.peek(template -> template.setContent(sub.replace(template.getContent())))
				.collect(Collectors.toList());
	}

	@Override
	public Set<String> getDirtyProperties() {
		Set<String> dirtyProperties = super.getDirtyProperties();
		if (this.getConfigurationTemplates().stream().anyMatch(ConfigurationTemplate::isDirty)) {
			dirtyProperties = Sets.union(dirtyProperties, Collections.singleton("configurationTemplates"));
		}
		return dirtyProperties;
	}

	public DesiredState getDesiredState() {
		return desiredState.get();
	}

	public void setDesiredState(DesiredState desiredState) {
		this.desiredState.set(desiredState);
	}
}
