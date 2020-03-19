package io.oneko.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.docker.DockerRegistry;
import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareListProperty;
import io.oneko.domain.ModificationAwareProperty;
import io.oneko.templates.WritableConfigurationTemplate;
import io.oneko.templates.ConfigurationTemplates;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * Project domain object. Each project has a number of different images that are run in kubernetes and organized through o-neko.
 */
@Slf4j
public class Project extends ModificationAwareIdentifiable {

	private final ModificationAwareProperty<UUID> uuid = new ModificationAwareProperty<>(this, "uuid");
	private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private final ModificationAwareProperty<String> imageName = new ModificationAwareProperty<>(this, "imageName");
	private final ModificationAwareProperty<DeploymentBehaviour> newVersionsDeploymentBehaviour = new ModificationAwareProperty<>(this, "newVersionsDeploymentBehaviour");
	/**
	 * Default configuration template to be used for versions. Should be a multi line yaml-string.
	 */
	private final ModificationAwareProperty<List<WritableConfigurationTemplate>> defaultConfigurationTemplates = new ModificationAwareListProperty<>(this, "defaultConfigurationTemplates");
	private final ModificationAwareProperty<DockerRegistry> dockerRegistry = new ModificationAwareProperty<>(this, "dockerRegistry");
	private final ModificationAwareProperty<LifetimeBehaviour> defaultLifetimeBehaviour = new ModificationAwareProperty<>(this, "defaultLifetimeBehaviour");
	private final List<TemplateVariable> templateVariables;
	private final List<ProjectVersion> versions;
	private final boolean newProject;

	/**
	 * Creates a completely new project
	 */
	public Project(DockerRegistry registry) {
		this.uuid.set(UUID.randomUUID());
		this.dockerRegistry.set(registry);
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
	public Project(UUID uuid, String name, String imageName, DeploymentBehaviour newVersionsDeploymentBehaviour,
				   List<WritableConfigurationTemplate> defaultConfigurationTemplates, List<TemplateVariable> templateVariables,
				   DockerRegistry dockerRegistry, List<ProjectVersion> versions, LifetimeBehaviour defaultLifetimeBehaviour) {
		this.uuid.init(uuid);
		this.name.init(name);
		this.imageName.init(imageName);
		this.newVersionsDeploymentBehaviour.init(newVersionsDeploymentBehaviour);
		this.defaultConfigurationTemplates.init(defaultConfigurationTemplates);
		this.dockerRegistry.init(dockerRegistry);
		this.defaultLifetimeBehaviour.init(defaultLifetimeBehaviour);
		this.versions = versions;
		this.templateVariables = templateVariables;
		this.newProject = false;
	}

	@Override
	public UUID getId() {
		return this.uuid.get();
	}

	public UUID getUuid() {
		return uuid.get();
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

	public List<TemplateVariable> getTemplateVariables() {
		return new ArrayList<>(this.templateVariables);
	}

	public void setTemplateVariables(List<TemplateVariable> variables) {
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

	/**
	 * Short hand method for retrieving the {@link DockerRegistry#getUuid()} of this project's {@link #getDockerRegistry()}.
	 */
	public UUID getDockerRegistryUuid() {
		if (this.isOrphan()) {
			return null;
		} else {
			return this.getDockerRegistry().getUuid();
		}
	}

	public DockerRegistry getDockerRegistry() {
		return dockerRegistry.get();
	}

	/**
	 * An orphaned project has no docker registry assigned. This happens, when a registry is getting deleted.
	 * No deployments can be performed on orphaned project's versions.
	 */
	public boolean isOrphan() {
		return this.dockerRegistry.get() == null;
	}

	public void assignToNewRegistry(DockerRegistry registry) {
		boolean registryChanged = this.dockerRegistry.set(registry);
		if (registryChanged) {
			//TODO...check whether images exist on new registry and remove them or do some other fancy shit...
			return;
		}
	}

	public ImmutableList<ProjectVersion> getVersions() {
		return ImmutableList.copyOf(this.versions);
	}

	public Optional<ProjectVersion> getVersionByUUID(UUID versionUUID) {
		return this.versions.stream()
				.filter(version -> version.getUuid().equals(versionUUID))
				.findAny();
	}

	public Optional<ProjectVersion> getVersionByName(String name) {
		return versions.stream()
				.filter(v -> StringUtils.equals(v.getName(), name))
				.findFirst();
	}

	/**
	 * Adds a new project version to this project.
	 */
	public ProjectVersion createVersion(String name) {
		if (hasVersion(name)) {
			return getVersionByName(name).get();
		}

		ProjectVersion version = new ProjectVersion(this, name);
		log.info("create version {} for project {}", name, getName());
		this.versions.add(version);
		return version;
	}

	/**
	 * Removes a project version from this project.
	 *
	 * @return the removed version - might be <code>null</code>
	 */
	public ProjectVersion removeVersion(String name) {
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
		if (this.templateVariables.stream().anyMatch(TemplateVariable::isDirty)) {
			dirtyProperties = Sets.union(dirtyProperties, Collections.singleton("templateVariables"));
		}
		if (this.versions.stream().anyMatch(ProjectVersion::isDirty)) {
			dirtyProperties = Sets.union(dirtyProperties, Collections.singleton("versions"));
		}
		if (this.getDefaultConfigurationTemplates().stream().anyMatch(WritableConfigurationTemplate::isDirty)) {
			dirtyProperties = Sets.union(dirtyProperties, Collections.singleton("defaultConfigurationTemplates"));
		}
		return dirtyProperties;
	}
}
