package io.oneko.project;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.domain.Identifiable;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.ImplicitNamespace;
import io.oneko.namespace.Namespace;
import io.oneko.templates.ReadableConfigurationTemplate;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static io.oneko.kubernetes.deployments.DesiredState.NotDeployed;

@Getter
public class ReadableProjectVersion extends Identifiable implements ProjectVersion<ReadableProject, ReadableProjectVersion> {

	private final UUID uuid;
	private ReadableProject project;
	private final String name;
	private final DeploymentBehaviour deploymentBehaviour;
	private final ImmutableMap<String, String> templateVariables;
	private final String dockerContentDigest;
	private final ImmutableList<String> urls;
	private final ImmutableList<ReadableConfigurationTemplate> configurationTemplates;
	private final boolean outdated;
	private final LifetimeBehaviour lifetimeBehaviour;
	private Namespace namespace;
	private final DesiredState desiredState;
	private final Instant imageUpdatedDate;

	@Builder
	public ReadableProjectVersion(UUID uuid, String name, DeploymentBehaviour deploymentBehaviour,
								  Map<String, String> templateVariables, String dockerContentDigest, List<String> urls,
								  List<ReadableConfigurationTemplate> configurationTemplates, boolean outdated, LifetimeBehaviour lifetimeBehaviour,
								  DefinedNamespace namespace, DesiredState desiredState, Instant imageUpdatedDate) {
		this.uuid = uuid;
		this.name = name;
		this.deploymentBehaviour = deploymentBehaviour;
		this.dockerContentDigest = dockerContentDigest;
		this.templateVariables = templateVariables == null ? ImmutableMap.of() : ImmutableMap.copyOf(templateVariables);
		this.urls = ImmutableList.copyOf(urls);
		this.outdated = outdated;
		this.configurationTemplates = configurationTemplates == null ? ImmutableList.of() : ImmutableList.copyOf(configurationTemplates);
		this.lifetimeBehaviour = lifetimeBehaviour;
		this.namespace = namespace;
		this.desiredState = Objects.requireNonNullElse(desiredState, NotDeployed);
		this.imageUpdatedDate = imageUpdatedDate;
	}

	//this here should only be called by the project
	void setProject(ReadableProject project) {
		Preconditions.checkArgument(this.project == null, "The project can not be set more than once");
		this.project = project;
		//The implicit namespace requires the project to be set
		if (this.namespace == null) {
			this.namespace = new ImplicitNamespace(this);
		}
	}

	@Override
	public UUID getId() {
		return getUuid();
	}

	public ReadableProject getProject() {
		return project;
	}

	public Optional<LifetimeBehaviour> getLifetimeBehaviour() {
		return Optional.ofNullable(lifetimeBehaviour);
	}

	WritableProjectVersion writable() {
		return WritableProjectVersion.builder()
				.uuid(getUuid())
				.name(getName())
				.deploymentBehaviour(getDeploymentBehaviour())
				.templateVariables(getTemplateVariables())
				.dockerContentDigest(getDockerContentDigest())
				.urls(getUrls())
				.configurationTemplates(getConfigurationTemplates().stream()
					.map(ReadableConfigurationTemplate::writable)
					.collect(Collectors.toList()))
				.outdated(isOutdated())
				.lifetimeBehaviour(lifetimeBehaviour)
				.namespace(namespace instanceof DefinedNamespace ? (DefinedNamespace)getNamespace() : null)
				.desiredState(getDesiredState())
				.imageUpdatedDate(getImageUpdatedDate())
				.build();
	}
}
