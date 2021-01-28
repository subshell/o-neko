package io.oneko.project;

import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.HasNamespace;
import io.oneko.namespace.Namespace;
import io.oneko.templates.ConfigurationTemplate;
import io.oneko.templates.ConfigurationTemplates;
import io.oneko.templates.WritableConfigurationTemplate;
import org.apache.commons.text.StringSubstitutor;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Each project has a number of versions. These versions are stored as part of the project they belong to.
 * As for all model entities, there are
 */
public interface ProjectVersion<P extends Project<P, V>, V extends ProjectVersion<P, V>> extends HasNamespace {

	UUID getId();

	P getProject();

	String getName();

	Optional<LifetimeBehaviour> getLifetimeBehaviour();

	default Optional<LifetimeBehaviour> getEffectiveLifetimeBehaviour() {
		return getLifetimeBehaviour().or(getProject()::getDefaultLifetimeBehaviour);
	}

	DeploymentBehaviour getDeploymentBehaviour();

	String getDockerContentDigest();

	List<String> getUrls();

	List<? extends ConfigurationTemplate> getConfigurationTemplates();

	boolean isOutdated();

	/**
	 * A project version is orphaned if it's project has no docker registry assigned. This happens, when a registry is getting deleted.
	 * No deployments can be performed on orphaned project versions.
	 */
	default boolean isOrphan() {
		return getProject().isOrphan();
	}

	default boolean isUpdatedAutomatically() {
		return getDeploymentBehaviour().equals(DeploymentBehaviour.automatically);
	}

	/**
	 * Provides a mutable copy of the template variables explicitly set on this version.
	 */
	Map<String, String> getTemplateVariables();

	default Map<String, String> getImplicitTemplateVariables() {
		Map<String, String> implicitTemplateVariables = new HashMap<>();
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.VERSION_NAME, getName());
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.ONEKO_VERSION, getId().toString());
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.SAFE_VERSION_NAME, safeVersionName());
		return implicitTemplateVariables;
	}

	default String safeVersionName() {
		final String safeName = getName().replaceAll("-?([^-\\w]|_)*-?", "").toLowerCase();
		return safeName.substring(0, Math.min(safeName.length(), 63));
	}

	/**
	 * Provides a mutable copy of all template variables retrieved by merging the ones from this version's
	 * {@link #getProject()} and this version's own {@link #getTemplateVariables()}.
	 *
	 * @return Never <code>null</code>
	 */
	default Map<String, String> calculateEffectiveTemplateVariables() {
		Map<String, String> mergedTemplateVariables = new HashMap<>();

		getProject().getTemplateVariables().
				forEach(var -> mergedTemplateVariables.put(var.getName(), var.getDefaultValue()));
		mergedTemplateVariables.putAll(getProject().getImplicitTemplateVariables());
		mergedTemplateVariables.putAll(getImplicitTemplateVariables());
		mergedTemplateVariables.putAll(getTemplateVariables());

		return mergedTemplateVariables;
	}

	/**
	 * Provides the effective template to use on this version. This is either derived from the project's configuration
	 * template or a modified version template and the effective template variables.
	 */
	default String calculateConfiguration() {
		return getCalculatedConfigurationTemplates().stream()
				.map(this::templateAsYAMLString)
				.collect(Collectors.joining("\n\n------\n\n"));
	}

	/**
	 * Provides all effective templates to use on this version. This is either derived from the project's configuration
	 * template or a modified version template and the effective template variables.
	 */
	default List<WritableConfigurationTemplate> getCalculatedConfigurationTemplates() {
		StringSubstitutor sub = new StringSubstitutor(this.calculateEffectiveTemplateVariables());

		//somehow java does not properly figure out the list type here
		final List<ConfigurationTemplate> unifiedTemplates = ConfigurationTemplates.unifyTemplateSets(getProject().getDefaultConfigurationTemplates(), getConfigurationTemplates());
		return unifiedTemplates.stream()
				.map(WritableConfigurationTemplate::clone)
				.peek(template -> template.setContent(sub.replace(template.getContent())))
				.collect(Collectors.toList());
	}

	Namespace getNamespace();

	/**
	 * Provides the ID of the defined namespace (if one is set.)
	 *
	 * @return might be <code>null</code>
	 */
	default UUID getDefinedNamespaceId() {
		return Optional.of(getNamespace())
				.filter(DefinedNamespace.class::isInstance)
				.map(namespace -> ((DefinedNamespace) namespace).getId())
				.orElse(null);
	}

	@Override
	default String getProtoNamespace() {
		return getProject().getName() + "-" + getName();
	}

	default String templateAsYAMLString(ConfigurationTemplate configurationTemplate) {
		return "# > " +
				configurationTemplate.getName() +
				" (" +
				configurationTemplate.getDescription() +
				")\n\n" +
				configurationTemplate.getContent();
	}

	DesiredState getDesiredState();

	Instant getImageUpdatedDate();

}
