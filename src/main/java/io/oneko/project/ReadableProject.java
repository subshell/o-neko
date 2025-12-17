package io.oneko.project;

import com.google.common.collect.ImmutableList;
import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.domain.Identifiable;
import io.oneko.templates.ReadableConfigurationTemplate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReadableProject extends Identifiable implements Project<ReadableProject, ReadableProjectVersion> {

	private final UUID id;
	private final String name;
	private final String imageName;
	private final DeploymentBehaviour newVersionsDeploymentBehaviour;
	private final ImmutableList<ReadableConfigurationTemplate> defaultConfigurationTemplates;
	private final UUID dockerRegistryId;
	private final LifetimeBehaviour defaultLifetimeBehaviour;
	private final ImmutableList<String> urlTemplates;
	private final ImmutableList<ReadableTemplateVariable> templateVariables;
	private final ImmutableList<ReadableProjectVersion> versions;
	private final String namespace;

	@Builder
	public ReadableProject(UUID id, String name, String imageName, DeploymentBehaviour newVersionsDeploymentBehaviour,
		List<ReadableConfigurationTemplate> defaultConfigurationTemplates,
		UUID dockerRegistryId, LifetimeBehaviour defaultLifetimeBehaviour,
		List<String> urlTemplates,
		List<ReadableTemplateVariable> templateVariables, List<ReadableProjectVersion> versions, String namespace) {
		this.id = id;
		this.name = name;
		this.imageName = imageName;
		this.newVersionsDeploymentBehaviour = newVersionsDeploymentBehaviour;
		this.urlTemplates = urlTemplates == null
			? ImmutableList.of()
			: ImmutableList.copyOf(urlTemplates.stream().filter(Objects::nonNull).collect(Collectors.toList()));
		this.defaultConfigurationTemplates = defaultConfigurationTemplates == null
			? ImmutableList.of()
			: ImmutableList.copyOf(defaultConfigurationTemplates.stream().filter(Objects::nonNull).collect(Collectors.toList()));
		this.dockerRegistryId = dockerRegistryId;
		this.defaultLifetimeBehaviour = defaultLifetimeBehaviour;
		this.templateVariables = templateVariables == null
			? ImmutableList.of()
			: ImmutableList.copyOf(templateVariables.stream().filter(Objects::nonNull).collect(Collectors.toList()));
		this.versions = versions == null ? ImmutableList.of() : ImmutableList.copyOf(versions.stream().filter(Objects::nonNull).collect(Collectors.toList()));
		this.versions.forEach(v -> v.setProject(this));
		this.namespace = namespace;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	public Optional<LifetimeBehaviour> getDefaultLifetimeBehaviour() {
		return Optional.ofNullable(this.defaultLifetimeBehaviour);
	}

	public WritableProject writable() {
		final List<WritableProjectVersion> versions = getVersions().stream()
			.map(ReadableProjectVersion::writable)
			.collect(Collectors.toList());
		return WritableProject.builder()
			.id(getId())
			.name(getName())
			.imageName(getImageName())
			.newVersionsDeploymentBehaviour(getNewVersionsDeploymentBehaviour())
			.urlTemplates(getUrlTemplates())
			.defaultConfigurationTemplates(getDefaultConfigurationTemplates().stream()
				.map(ReadableConfigurationTemplate::writable)
				.collect(Collectors.toList()))
			.dockerRegistryId(getDockerRegistryId())
			.defaultLifetimeBehaviour(defaultLifetimeBehaviour)
			.templateVariables(getTemplateVariables().stream()
				.map(ReadableTemplateVariable::writable)
				.collect(Collectors.toList()))
			.versions(versions)
			.namespace(getNamespace())
			.build();
	}
}
