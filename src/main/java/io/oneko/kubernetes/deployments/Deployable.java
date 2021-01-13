package io.oneko.kubernetes.deployments;

import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeployableConfigurationTemplates;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.project.Project;
import io.oneko.project.ProjectVersion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Deployable is a wrapper around entities that can be deployed to kubernetes.
 * Provides access to all relevant data and adds in handling for tags and labels.
 *
 * @param <T>
 */
public interface Deployable<T> {

	T getEntity();

	UUID getId();

	String getName();

	String getFullLabel();

	UUID getDockerRegistryId();

	Project<?, ?> getRelatedProject();

	ProjectVersion<?, ?> getRelatedProjectVersion();

	DeployableConfigurationTemplates getConfigurationTemplates();

	boolean isOutdated();

	void setOutdated(boolean outdated);

	List<String> getUrls();

	void setUrls(List<String> urls);

	String getDockerContentDigest();

	void setDockerContentDigest(String dockerContentDigest);

	Optional<LifetimeBehaviour> calculateEffectiveLifetimeBehaviour();

	DeploymentBehaviour getDeploymentBehaviour();

	Map.Entry<String, String> getPrimaryLabel();

	DesiredState getDesiredState();

	void setDesiredState(DesiredState desiredState);
}
