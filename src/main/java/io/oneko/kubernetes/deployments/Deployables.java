package io.oneko.kubernetes.deployments;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.project.ProjectConstants;
import io.oneko.project.WritableProject;
import io.oneko.project.WritableProjectVersion;
import io.oneko.templates.ConfigurationTemplate;
import io.oneko.templates.WritableConfigurationTemplate;

public class Deployables {

	public static Deployable<WritableProjectVersion> of(WritableProjectVersion version) {
		return new Deployable<>() {

			@Override
			public WritableProjectVersion getEntity() {
				return version;
			}

			@Override
			public UUID getId() {
				return version.getId();
			}

			@Override
			public String getName() {
				return version.getName();
			}

			@Override
			public String getFullLabel() {
				return "Version " + version.getName() + " of project " + version.getProject().getName();
			}

			@Override
			public UUID getDockerRegistryId() {
				return version.getProject().getDockerRegistryId();
			}

			public WritableProject getRelatedProject() {
				return version.getProject();
			}

			public WritableProjectVersion getRelatedProjectVersion() {
				return version;
			}

			@Override
			public List<ConfigurationTemplate> getConfigurationTemplates() {
				return version.getCalculatedConfigurationTemplates().stream().map(WritableConfigurationTemplate::readable).collect(Collectors.toList());
			}

			@Override
			public boolean isOutdated() {
				return version.isOutdated();
			}

			@Override
			public List<String> getUrls() {
				return version.getUrls();
			}

			@Override
			public void setOutdated(boolean outdated) {
				version.setOutdated(outdated);
			}

			@Override
			public String getDockerContentDigest() {
				return version.getDockerContentDigest();
			}

			@Override
			public Optional<LifetimeBehaviour> calculateEffectiveLifetimeBehaviour() {
				return version.getEffectiveLifetimeBehaviour();
			}

			@Override
			public void setUrls(List<String> urls) {
				version.setUrls(urls);
			}

			@Override
			public DeploymentBehaviour getDeploymentBehaviour() {
				return version.getDeploymentBehaviour();
			}

			@Override
			public Map.Entry<String, String> getPrimaryLabel() {
				return Pair.of(ProjectConstants.TemplateVariablesNames.ONEKO_VERSION, version.getId().toString());
			}			@Override
			public void setDockerContentDigest(String dockerContentDigest) {
				version.setDockerContentDigest(dockerContentDigest);
			}

			@Override
			public DesiredState getDesiredState() {
				return version.getDesiredState();
			}

			@Override
			public void setDesiredState(DesiredState desiredState) {
				version.setDesiredState(desiredState);
			}
		};
	}
}
