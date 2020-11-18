package io.oneko.projectmesh;

import com.google.common.collect.Sets;
import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareListProperty;
import io.oneko.domain.ModificationAwareMapProperty;
import io.oneko.domain.ModificationAwareProperty;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.templates.ConfigurationTemplates;
import io.oneko.templates.WritableConfigurationTemplate;
import lombok.Builder;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

public class WritableMeshComponent extends ModificationAwareIdentifiable implements MeshComponent<WritableProjectMesh, WritableMeshComponent> {

	@Getter
	private WritableProjectMesh owner;
	private final ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
	private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private final UUID projectId;
	private final ModificationAwareProperty<UUID> projectVersionId = new ModificationAwareProperty<>(this, "projectVersion");
	private final ModificationAwareProperty<String> dockerContentDigest = new ModificationAwareProperty<>(this, "dockerContentDigest");
	private final ModificationAwareProperty<Map<String, String>> templateVariables = new ModificationAwareMapProperty<>(this, "templateVariables");
	private final ModificationAwareProperty<List<WritableConfigurationTemplate>> configurationTemplates = new ModificationAwareListProperty<>(this, "configurationTemplates");
	private final ModificationAwareProperty<Boolean> outdated = new ModificationAwareProperty<>(this, "outdated");
	private final ModificationAwareProperty<List<String>> urls = new ModificationAwareListProperty<>(this, "urls");
	private final ModificationAwareProperty<DesiredState> desiredState = new ModificationAwareProperty<>(this, "desiredState");

	@Builder
	public WritableMeshComponent(UUID id, String name, UUID projectId,
								 UUID projectVersionId, String dockerContentDigest,
								 Map<String, String> templateVariables,
								 List<WritableConfigurationTemplate> configurationTemplates, boolean outdated,
								 List<String> urls, DesiredState desiredState) {
		this.id.init(id);
		this.name.init(name);
		this.projectId = projectId;
		this.projectVersionId.init(projectVersionId);
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
	WritableMeshComponent(WritableProjectMesh owner, UUID projectId, UUID projectVersionId) {
		this.owner = Objects.requireNonNull(owner);
		this.id.set(UUID.randomUUID());
		this.projectId = Objects.requireNonNull(projectId);
		this.projectVersionId.set(Objects.requireNonNull(projectVersionId));
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

	@Override
	public UUID getProjectId() {
		return projectId;
	}

	@Override
	public UUID getProjectVersionId() {
		return projectVersionId.get();
	}

	public void setProjectVersion(UUID version) {
		this.projectVersionId.set(version);
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
				.projectId(getProjectId())
				.projectVersionId(getProjectVersionId())
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
