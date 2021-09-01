package io.oneko.project;

import static io.oneko.kubernetes.deployments.DesiredState.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Sets;

import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareListProperty;
import io.oneko.domain.ModificationAwareMapProperty;
import io.oneko.domain.ModificationAwareProperty;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.templates.ConfigurationTemplates;
import io.oneko.templates.WritableConfigurationTemplate;
import lombok.Builder;

public class WritableProjectVersion extends ModificationAwareIdentifiable implements ProjectVersion<WritableProject, WritableProjectVersion> {

	private final ModificationAwareProperty<UUID> uuid = new ModificationAwareProperty<>(this, "uuid");
	private WritableProject project;
	private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private final ModificationAwareProperty<DeploymentBehaviour> deploymentBehaviour = new ModificationAwareProperty<>(this, "deploymentBehaviour");
	private final ModificationAwareProperty<Map<String, String>> templateVariables = new ModificationAwareMapProperty<>(this, "templateVariables");
	private final ModificationAwareProperty<String> dockerContentDigest = new ModificationAwareProperty<>(this, "dockerContentDigest");
	private final ModificationAwareProperty<List<String>> urls = new ModificationAwareListProperty<>(this, "urls");
	private final ModificationAwareProperty<List<String>> urlTemplates = new ModificationAwareListProperty<>(this, "urlTemplates");
	private final ModificationAwareProperty<List<WritableConfigurationTemplate>> configurationTemplates = new ModificationAwareListProperty<>(this, "configurationTemplates");
	private final ModificationAwareProperty<Boolean> outdated = new ModificationAwareProperty<>(this, "outdated");
	private final ModificationAwareProperty<LifetimeBehaviour> lifetimeBehaviour = new ModificationAwareProperty<>(this, "lifetimeBehaviour");
	private final ModificationAwareProperty<String> namespace = new ModificationAwareProperty<>(this, "namespace");
	private final ModificationAwareProperty<DesiredState> desiredState = new ModificationAwareProperty<>(this, "desiredState");
	private final ModificationAwareProperty<Instant> imageUpdatedDate = new ModificationAwareProperty<>(this, "imageUpdatedDate");

	@Builder
	public WritableProjectVersion(UUID uuid, String name, DeploymentBehaviour deploymentBehaviour,
																Map<String, String> templateVariables, String dockerContentDigest, List<String> urls,
																List<String> urlTemplates, List<WritableConfigurationTemplate> configurationTemplates,
																boolean outdated, LifetimeBehaviour lifetimeBehaviour, String namespace,
																DesiredState desiredState, Instant imageUpdatedDate) {
		this.uuid.init(uuid);
		this.name.init(name);
		this.deploymentBehaviour.init(deploymentBehaviour);
		this.dockerContentDigest.init(dockerContentDigest);
		this.templateVariables.init(templateVariables);
		this.urls.init(urls);
		this.outdated.init(outdated);
		this.urlTemplates.init(urlTemplates);
		initUrlsFromUrlTemplates();
		this.configurationTemplates.init(configurationTemplates);
		this.lifetimeBehaviour.init(lifetimeBehaviour);
		this.namespace.init(namespace);
		this.desiredState.init(Objects.requireNonNullElse(desiredState, NotDeployed));
		this.imageUpdatedDate.init(imageUpdatedDate);
	}

	/**
	 * Should only be called by the project after instantiation.
	 */
	void setProject(WritableProject project) {
		this.project = project;
		initUrlsFromUrlTemplates();
	}

	/**
	 * Creates a new version of the given project.
	 */
	WritableProjectVersion(WritableProject project, String name) {
		this.uuid.set(UUID.randomUUID());
		this.project = Objects.requireNonNull(project);
		this.name.set(name);
		this.outdated.set(false);
		this.deploymentBehaviour.set(project.getNewVersionsDeploymentBehaviour());
		this.desiredState.set(NotDeployed);
	}

	@Override
	public UUID getId() {
		return this.uuid.get();
	}

	public UUID getUuid() {
		return uuid.get();
	}

	@JsonIgnore
	public WritableProject getProject() {
		return project;
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public Optional<LifetimeBehaviour> getLifetimeBehaviour() {
		return Optional.ofNullable(lifetimeBehaviour.get());
	}

	public void setLifetimeBehaviour(LifetimeBehaviour lifetimeBehaviour) {
		this.lifetimeBehaviour.set(lifetimeBehaviour);
	}

	public DeploymentBehaviour getDeploymentBehaviour() {
		return deploymentBehaviour.get();
	}

	public void setDeploymentBehaviour(DeploymentBehaviour deploymentBehaviour) {
		this.deploymentBehaviour.set(deploymentBehaviour);
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

	@Override
	public List<String> getUrlTemplates() {
		return urlTemplates.get();
	}

	public void setUrlTemplates(List<String> urlTemplates) {
		this.urlTemplates.set(urlTemplates);
		setUrlsFromUrlTemplates();
	}

	public void initUrlsFromUrlTemplates() {
		this.urls.init(Arrays.asList(getCalculatedUrls()));
	}

	void setUrlsFromUrlTemplates() {
		this.urls.set(Arrays.asList(getCalculatedUrls()));
	}

	public List<WritableConfigurationTemplate> getConfigurationTemplates() {
		return this.configurationTemplates.get();
	}

	public void setConfigurationTemplates(List<WritableConfigurationTemplate> configurationTemplates) {
		ConfigurationTemplates.ensureConsistentCollection(configurationTemplates);
		this.configurationTemplates.set(configurationTemplates);
	}

	public boolean isOutdated() {
		return this.outdated.get();
	}

	public void setOutdated(boolean outdated) {
		this.outdated.set(outdated);
	}

	public boolean isUpdatedAutomatically() {
		return getDeploymentBehaviour().equals(DeploymentBehaviour.automatically);
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

	public String getNamespace() {
		return namespace.get();
	}

	public void setNamespace(String namespace) {
		this.namespace.set(namespace);
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

	public Instant getImageUpdatedDate() {
		return imageUpdatedDate.get();
	}

	public void setImageUpdatedDate(Instant imageUpdatedDate) {
		this.imageUpdatedDate.set(imageUpdatedDate);
	}

	ReadableProjectVersion readable() {
		return ReadableProjectVersion.builder()
				.uuid(getUuid())
				.name(getName())
				.deploymentBehaviour(getDeploymentBehaviour())
				.templateVariables(getTemplateVariables())
				.dockerContentDigest(getDockerContentDigest())
				.urls(getUrls())
				.urlTemplates(getUrlTemplates())
				.configurationTemplates(getConfigurationTemplates().stream()
						.map(WritableConfigurationTemplate::readable)
						.collect(Collectors.toList()))
				.outdated(isOutdated())
				.lifetimeBehaviour(lifetimeBehaviour.get())
				.namespace(getNamespace())
				.desiredState(getDesiredState())
				.imageUpdatedDate(getImageUpdatedDate())
				.build();
	}
}
