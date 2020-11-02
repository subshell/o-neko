package io.oneko.project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.templates.ConfigurationTemplate;

public interface Project<P extends Project<P, V>, V extends ProjectVersion<P, V>> {

	UUID getId();

	String getName();

	Optional<LifetimeBehaviour> getDefaultLifetimeBehaviour();

	String getImageName();

	DeploymentBehaviour getNewVersionsDeploymentBehaviour();

	List<? extends ConfigurationTemplate> getDefaultConfigurationTemplates();

	List<? extends TemplateVariable> getTemplateVariables();

	default Map<String, String> getImplicitTemplateVariables() {
		Map<String, String> implicitTemplateVariables = new HashMap<>();
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.PROJECT_NAME, this.getName());
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.ONEKO_PROJECT, this.getId().toString());
		return implicitTemplateVariables;
	}

	UUID getDockerRegistryId();


	/**
	 * An orphaned project has no docker registry assigned. This happens, when a registry is getting deleted.
	 * No deployments can be performed on orphaned project's versions.
	 */
	default boolean isOrphan() {
		return this.getDockerRegistryId() == null;
	}

	List<V> getVersions();

	default Optional<V> getVersionById(UUID versionId) {
		return getVersions().stream()
				.filter(version -> version.getId().equals(versionId))
				.findAny();
	}

	default Optional<V> getVersionByName(String name) {
		return getVersions().stream()
				.filter(v -> StringUtils.equals(v.getName(), name))
				.findFirst();
	}

	default boolean hasVersion(String name) {
		return getVersionByName(name).isPresent();
	}
}
