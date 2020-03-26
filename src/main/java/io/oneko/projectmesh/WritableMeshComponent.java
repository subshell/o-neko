package io.oneko.projectmesh;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareListProperty;
import io.oneko.domain.ModificationAwareMapProperty;
import io.oneko.domain.ModificationAwareProperty;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.project.ReadableProject;
import io.oneko.project.ReadableProjectVersion;
import io.oneko.templates.WritableConfigurationTemplate;
import io.oneko.templates.ConfigurationTemplates;
import lombok.Builder;
import lombok.Getter;

public class WritableMeshComponent extends ModificationAwareIdentifiable implements MeshComponent<WritableProjectMesh, WritableMeshComponent> {

	@Getter
	private WritableProjectMesh owner;
	private final ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
	private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private final ReadableProject project;
	private final ModificationAwareProperty<ReadableProjectVersion> projectVersion = new ModificationAwareProperty<>(this, "projectVersion");
	private final ModificationAwareProperty<String> dockerContentDigest = new ModificationAwareProperty<>(this, "dockerContentDigest");
	private final ModificationAwareProperty<Map<String, String>> templateVariables = new ModificationAwareMapProperty<>(this, "templateVariables");
	private final ModificationAwareProperty<List<WritableConfigurationTemplate>> configurationTemplates = new ModificationAwareListProperty<>(this, "configurationTemplates");
	private final ModificationAwareProperty<Boolean> outdated = new ModificationAwareProperty<>(this, "outdated");
	private final ModificationAwareProperty<List<String>> urls = new ModificationAwareListProperty<>(this, "urls");
	private final ModificationAwareProperty<DesiredState> desiredState = new ModificationAwareProperty<>(this, "desiredState");

	@Builder
	public WritableMeshComponent(UUID id, String name, ReadableProject project,
								 ReadableProjectVersion projectVersion, String dockerContentDigest,
								 Map<String, String> templateVariables,
								 List<WritableConfigurationTemplate> configurationTemplates, boolean outdated,
								 List<String> urls, DesiredState desiredState) {
		this.id.init(id);
		this.name.init(name);
		this.project = project;
		this.projectVersion.init(projectVersion);
		this.dockerContentDigest.init(dockerContentDigest);
		this.templateVariables.init(templateVariables);
		this.configurationTemplates.init(configurationTemplates);
		this.outdated.init(outdated);
		this.urls.init(urls);
		this.desiredState.init(Objects.requireNonNullElse(desiredState, DesiredState.NotDeployed));
	}

	//only to be called by the owner
	void setOwner(WritableProjectMesh owner) {
		this.owner = owner;
	}

	/**
	 * Creates a mesh of the given project.
	 */
	WritableMeshComponent(WritableProjectMesh owner, ReadableProject project, ReadableProjectVersion version) {
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

	public ReadableProject getProject() {
		return project;
	}

	public ReadableProjectVersion getProjectVersion() {
		return this.projectVersion.get();
	}

	public void setProjectVersion(ReadableProjectVersion version) {
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

	public List<WritableConfigurationTemplate> getConfigurationTemplates() {
		return this.configurationTemplates.get();
	}

	public void setConfigurationTemplates(List<WritableConfigurationTemplate> configurationTemplates) {
		ConfigurationTemplates.ensureConsistentCollection(configurationTemplates);
		this.configurationTemplates.set(configurationTemplates);
	}

	@Override
	public Set<String> getDirtyProperties() {
		Set<String> dirtyProperties = super.getDirtyProperties();
		if (this.getConfigurationTemplates().stream().anyMatch(WritableConfigurationTemplate::isDirty)) {
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

	ReadableMeshComponent readable() {
		return ReadableMeshComponent.builder()
				.id(getId())
				.name(getName())
				.project(getProject())
				.projectVersion(getProjectVersion())
				.dockerContentDigest(getDockerContentDigest())
				.templateVariables(getTemplateVariables())
				.configurationTemplates(getConfigurationTemplates().stream()
						.map(WritableConfigurationTemplate::readable)
						.collect(Collectors.toList()))
				.outdated(isOutdated())
				.urls(getUrls())
				.desiredState(getDesiredState())
				.build();
	}
}
