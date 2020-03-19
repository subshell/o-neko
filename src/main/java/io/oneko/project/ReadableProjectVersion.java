package io.oneko.project;

import static io.oneko.kubernetes.deployments.DesiredState.NotDeployed;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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

@Getter
public class ReadableProjectVersion extends Identifiable implements ProjectVersion {

	private final UUID uuid;
	private final Project project;
	private final String name;
	private final DeploymentBehaviour deploymentBehaviour;
	private final ImmutableMap<String, String> templateVariables;
	private final String dockerContentDigest;
	private final ImmutableList<String> urls;
	private final ImmutableList<ReadableConfigurationTemplate> configurationTemplates;
	private final Boolean outdated;
	private final LifetimeBehaviour lifetimeBehaviour;
	private final Namespace namespace;
	private final DesiredState desiredState;
	private final Instant imageUpdatedDate;

	@Builder
	public ReadableProjectVersion(UUID uuid, Project project, String name, DeploymentBehaviour deploymentBehaviour,
								  Map<String, String> templateVariables, String dockerContentDigest, List<String> urls,
								  List<ReadableConfigurationTemplate> configurationTemplates, boolean outdated, LifetimeBehaviour lifetimeBehaviour,
								  DefinedNamespace namespace, DesiredState desiredState, Instant imageUpdatedDate) {
		this.uuid = uuid;
		this.project = project;
		this.name = name;
		this.deploymentBehaviour = deploymentBehaviour;
		this.dockerContentDigest = dockerContentDigest;
		this.templateVariables = ImmutableMap.copyOf(templateVariables);
		this.urls = ImmutableList.copyOf(urls);
		this.outdated = outdated;
		this.configurationTemplates = ImmutableList.copyOf(configurationTemplates);
		this.lifetimeBehaviour = lifetimeBehaviour;
		this.namespace = Objects.requireNonNullElse(namespace, new ImplicitNamespace(this));
		this.desiredState = Objects.requireNonNullElse(desiredState, NotDeployed);
		this.imageUpdatedDate = imageUpdatedDate;
	}

	@Override
	public UUID getId() {
		return getUuid();
	}

	public WritableProjectVersion writable() {
		return WritableProjectVersion.builder()
				.uuid(getUuid())
				.project(getProject())
				.name(getName())
				.deploymentBehaviour(getDeploymentBehaviour())
				.templateVariables(getTemplateVariables())
				.dockerContentDigest(getDockerContentDigest())
				.urls(getUrls())
				.configurationTemplates(getConfigurationTemplates().stream()
					.map(ReadableConfigurationTemplate::writable)
					.collect(Collectors.toList()))
				.outdated(getOutdated())
				.lifetimeBehaviour(getLifetimeBehaviour())
				.namespace(getNamespace() instanceof DefinedNamespace ? (DefinedNamespace)getNamespace() : null)
				.desiredState(getDesiredState())
				.imageUpdatedDate(getImageUpdatedDate())
				.build();
	}
}
