package io.oneko.projectmesh;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.project.ReadableProject;
import io.oneko.project.ReadableProjectVersion;
import io.oneko.templates.ReadableConfigurationTemplate;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReadableMeshComponent implements MeshComponent<ReadableProjectMesh, ReadableMeshComponent> {
	private ReadableProjectMesh owner;
	private final UUID id;
	private final String name;
	private final ReadableProject project;
	private final ReadableProjectVersion projectVersion;
	private final String dockerContentDigest;
	private final ImmutableMap<String, String> templateVariables;
	private final ImmutableList<ReadableConfigurationTemplate> configurationTemplates;
	private final boolean outdated;
	private final ImmutableList<String> urls;
	private final DesiredState desiredState;

	@Builder
	public ReadableMeshComponent(UUID id, String name, ReadableProject project, ReadableProjectVersion projectVersion,
								 String dockerContentDigest, Map<String, String> templateVariables,
								 List<ReadableConfigurationTemplate> configurationTemplates, boolean outdated,
								 List<String> urls, DesiredState desiredState) {
		this.id = id;
		this.name = name;
		this.project = project;
		this.projectVersion = projectVersion;
		this.dockerContentDigest = dockerContentDigest;
		this.templateVariables = ImmutableMap.copyOf(templateVariables);
		this.configurationTemplates = ImmutableList.copyOf(configurationTemplates);
		this.outdated = outdated;
		this.urls = ImmutableList.copyOf(urls);
		this.desiredState = desiredState;
	}

	//only to be called by the owner
	void setOwner(ReadableProjectMesh owner) {
		this.owner = owner;
	}

	WritableMeshComponent writable() {
		return WritableMeshComponent.builder()
				.id(getId())
				.name(getName())
				.project(getProject())
				.projectVersion(getProjectVersion())
				.dockerContentDigest(getDockerContentDigest())
				.templateVariables(getTemplateVariables())
				.configurationTemplates(getConfigurationTemplates().stream()
					.map(ReadableConfigurationTemplate::writable)
					.collect(Collectors.toList()))
				.outdated(isOutdated())
				.urls(getUrls())
				.desiredState(getDesiredState())
				.build();
	}
}
