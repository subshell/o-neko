package io.oneko.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareListProperty;
import io.oneko.domain.ModificationAwareProperty;
import io.oneko.namespace.Namespace;
import io.oneko.namespace.WritableNamespace;
import io.oneko.templates.ConfigurationTemplates;
import io.oneko.templates.WritableConfigurationTemplate;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * Project domain object. Each project has a number of different images that are run in kubernetes and organized through o-neko.
 */
@Slf4j
public class WritableProject extends ModificationAwareIdentifiable implements Project<WritableProject, WritableProjectVersion> {

	private final ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
	private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private final ModificationAwareProperty<String> imageName = new ModificationAwareProperty<>(this, "imageName");
	private final ModificationAwareProperty<DeploymentBehaviour> newVersionsDeploymentBehaviour = new ModificationAwareProperty<>(this, "newVersionsDeploymentBehaviour");
	/**
	 * Default configuration template to be used for versions. Should be a multi line yaml-string.
	 */
	private final ModificationAwareProperty<List<WritableConfigurationTemplate>> defaultConfigurationTemplates = new ModificationAwareListProperty<>(this, "defaultConfigurationTemplates");
	private final ModificationAwareProperty<UUID> dockerRegistryId = new ModificationAwareProperty<>(this, "dockerRegistry");
	private final ModificationAwareProperty<LifetimeBehaviour> defaultLifetimeBehaviour = new ModificationAwareProperty<>(this, "defaultLifetimeBehaviour");
	private final ModificationAwareProperty<String> namespace = new ModificationAwareProperty<>(this, "namespace");
	private final List<WritableTemplateVariable> templateVariables;
	private final List<WritableProjectVersion> versions;
	private final boolean newProject;

	/**
	 * Creates a completely new project
	 */
	public WritableProject(UUID dockerRegistryId) {
		this.id.set(UUID.randomUUID());
		this.dockerRegistryId.set(dockerRegistryId);
		this.versions = new ArrayList<>();
		this.templateVariables = new ArrayList<>();
		this.newVersionsDeploymentBehaviour.set(DeploymentBehaviour.automatically);
		this.newProject = true;
	}

	/**
	 * Creates an existing DockerInfo object.
	 * This constructor is intended to be used by persistence layers only.
	 */
	@Builder
	public WritableProject(UUID id, String name, String imageName, DeploymentBehaviour newVersionsDeploymentBehaviour,
						   List<WritableConfigurationTemplate> defaultConfigurationTemplates, List<WritableTemplateVariable> templateVariables,
						   UUID dockerRegistryId, List<WritableProjectVersion> versions, LifetimeBehaviour defaultLifetimeBehaviour, String namespace) {
		this.id.init(id);
		this.name.init(name);
		this.imageName.init(imageName);
		this.newVersionsDeploymentBehaviour.init(newVersionsDeploymentBehaviour);
		this.defaultConfigurationTemplates.init(defaultConfigurationTemplates);
		this.dockerRegistryId.init(dockerRegistryId);
		this.defaultLifetimeBehaviour.init(defaultLifetimeBehaviour);
		this.templateVariables = templateVariables;
		this.newProject = false;
		this.versions = versions == null ? new ArrayList<>() : new ArrayList<>(versions);
		this.versions.forEach(v -> v.setProject(this));
		this.namespace.init(namespace);
	}

	@Override
	public UUID getId() {
		return this.id.get();
	}

	public String getNamespace() {
		return namespace.get();
	}

	public void setNamespace(String namespace) {
		this.namespace.set(namespace);
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public Optional<LifetimeBehaviour> getDefaultLifetimeBehaviour() {
		return Optional.ofNullable(this.defaultLifetimeBehaviour.get());
	}

	public void setDefaultLifetimeBehaviour(LifetimeBehaviour defaultLifetimeBehaviour) {
		this.defaultLifetimeBehaviour.set(defaultLifetimeBehaviour);
	}

	public String getImageName() {
		return imageName.get();
	}

	public void setImageName(String imageName) {
		this.imageName.set(imageName);
	}

	public DeploymentBehaviour getNewVersionsDeploymentBehaviour() {
		return newVersionsDeploymentBehaviour.get();
	}

	public void setNewVersionsDeploymentBehaviour(DeploymentBehaviour newVersionsDeploymentBehaviour) {
		this.newVersionsDeploymentBehaviour.set(newVersionsDeploymentBehaviour);
	}

	public List<WritableConfigurationTemplate> getDefaultConfigurationTemplates() {
		return defaultConfigurationTemplates.get();
	}

	public void setDefaultConfigurationTemplates(List<WritableConfigurationTemplate> defaultConfigurationTemplates) {
		ConfigurationTemplates.ensureConsistentCollection(defaultConfigurationTemplates);
		this.defaultConfigurationTemplates.set(defaultConfigurationTemplates);
	}

	public List<WritableTemplateVariable> getTemplateVariables() {
		return new ArrayList<>(this.templateVariables);
	}

	public void setTemplateVariables(List<WritableTemplateVariable> variables) {
		this.touchProperty("templateVariables");
		this.templateVariables.clear();
		this.templateVariables.addAll(variables);
	}

	public Map<String, String> getImplicitTemplateVariables() {
		Map<String, String> implicitTemplateVariables = new HashMap<>();
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.PROJECT_NAME, this.getName());
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.ONEKO_PROJECT, this.getId().toString());
		return implicitTemplateVariables;
	}

	public UUID getDockerRegistryId() {
		return this.dockerRegistryId.get();
	}

	public void assignToNewRegistry(UUID registryId) {
		boolean registryChanged = this.dockerRegistryId.set(registryId);
		if (registryChanged) {
			//TODO...check whether images exist on new registry and remove them or do some other fancy shit...
			return;
		}
	}

	public ImmutableList<WritableProjectVersion> getVersions() {
		return ImmutableList.copyOf(this.versions);
	}

	/**
	 * Adds a new project version to this project.
	 */
	public WritableProjectVersion createVersion(String name) {
		if (hasVersion(name)) {
			return getVersionByName(name).get();
		}

		WritableProjectVersion version = new WritableProjectVersion(this, name);
		log.info("create version {} for project {}", name, getName());
		this.versions.add(version);
		return version;
	}

	/**
	 * Removes a project version from this project.
	 *
	 * @return the removed version - might be <code>null</code>
	 */
	public WritableProjectVersion removeVersion(String name) {
		for (int i = 0; i < versions.size(); i++) {
			if (StringUtils.equals(versions.get(i).getName(), name)) {
				this.touchProperty("versions");
				return versions.remove(i);
			}
		}
		return null;
	}

	public boolean hasVersion(String name) {
		return getVersionByName(name).isPresent();
	}

	@Override
	public Set<String> getDirtyProperties() {
		Set<String> dirtyProperties = super.getDirtyProperties();
		if (this.templateVariables.stream().anyMatch(WritableTemplateVariable::isDirty)) {
			dirtyProperties = Sets.union(dirtyProperties, Collections.singleton("templateVariables"));
		}
		if (this.versions.stream().anyMatch(WritableProjectVersion::isDirty)) {
			dirtyProperties = Sets.union(dirtyProperties, Collections.singleton("versions"));
		}
		if (this.getDefaultConfigurationTemplates().stream().anyMatch(WritableConfigurationTemplate::isDirty)) {
			dirtyProperties = Sets.union(dirtyProperties, Collections.singleton("defaultConfigurationTemplates"));
		}
		return dirtyProperties;
	}

	public ReadableProject readable() {
		final List<ReadableProjectVersion> versions = getVersions().stream()
				.map(WritableProjectVersion::readable)
				.collect(Collectors.toList());
		return ReadableProject.builder()
				.id(getId())
				.name(getName())
				.imageName(getImageName())
				.newVersionsDeploymentBehaviour(getNewVersionsDeploymentBehaviour())
				.defaultConfigurationTemplates(getDefaultConfigurationTemplates().stream()
						.map(WritableConfigurationTemplate::readable)
						.collect(Collectors.toList()))
				.dockerRegistryId(getDockerRegistryId())
				.defaultLifetimeBehaviour(defaultLifetimeBehaviour.get())
				.templateVariables(getTemplateVariables().stream()
						.map(WritableTemplateVariable::readable)
						.collect(Collectors.toList()))
				.versions(versions)
				.build();
	}
}
