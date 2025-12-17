package io.oneko.project;

import static io.oneko.kubernetes.deployments.DesiredState.NotDeployed;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.domain.Identifiable;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.templates.ReadableConfigurationTemplate;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReadableProjectVersion extends Identifiable implements ProjectVersion<ReadableProject, ReadableProjectVersion> {

	private final UUID uuid;
	private ReadableProject project;
	private final String name;
	private final DeploymentBehaviour deploymentBehaviour;
	private final ImmutableMap<String, String> templateVariables;
	private final String dockerContentDigest;
	private final ImmutableList<String> urlTemplates;
	private final ImmutableList<String> urls;
	private final ImmutableList<ReadableConfigurationTemplate> configurationTemplates;
	private final boolean outdated;
	private final LifetimeBehaviour lifetimeBehaviour;
	private final String namespace;
	private final DesiredState desiredState;
	private final Instant imageUpdatedDate;

	@Builder
	public ReadableProjectVersion(UUID uuid, String name, DeploymentBehaviour deploymentBehaviour,
		Map<String, String> templateVariables, String dockerContentDigest, List<String> urls,
		List<String> urlTemplates, List<ReadableConfigurationTemplate> configurationTemplates,
		boolean outdated, LifetimeBehaviour lifetimeBehaviour, String namespace,
		DesiredState desiredState, Instant imageUpdatedDate) {
		this.uuid = uuid;
		this.name = name;
		this.deploymentBehaviour = deploymentBehaviour;
		this.dockerContentDigest = dockerContentDigest;
		this.templateVariables = templateVariables == null
			? ImmutableMap.of()
			: ImmutableMap.copyOf(templateVariables.entrySet().stream()
				.filter(e -> e.getKey() != null && e.getValue() != null)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
			);
		this.urls = urls == null ? ImmutableList.of() : ImmutableList.copyOf(urls.stream().filter(Objects::nonNull).collect(Collectors.toList()));
		this.outdated = outdated;
		this.urlTemplates = urlTemplates == null
			? ImmutableList.of()
			: ImmutableList.copyOf(urlTemplates.stream().filter(Objects::nonNull).collect(Collectors.toList()));
		this.configurationTemplates = configurationTemplates == null
			? ImmutableList.of()
			: ImmutableList.copyOf(configurationTemplates.stream().filter(Objects::nonNull).collect(Collectors.toList()));
		this.lifetimeBehaviour = lifetimeBehaviour;
		this.namespace = namespace;
		this.desiredState = Objects.requireNonNullElse(desiredState, NotDeployed);
		this.imageUpdatedDate = imageUpdatedDate;
	}

	//this here should only be called by the project
	void setProject(ReadableProject project) {
		Preconditions.checkArgument(this.project == null, "The project can not be set more than once");
		this.project = project;
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
			.urlTemplates(getUrlTemplates())
			.configurationTemplates(getConfigurationTemplates().stream()
				.map(ReadableConfigurationTemplate::writable)
				.collect(Collectors.toList()))
			.outdated(isOutdated())
			.lifetimeBehaviour(lifetimeBehaviour)
			.namespace(getNamespace())
			.desiredState(getDesiredState())
			.imageUpdatedDate(getImageUpdatedDate())
			.build();
	}
}
