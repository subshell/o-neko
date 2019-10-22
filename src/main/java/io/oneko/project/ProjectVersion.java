package io.oneko.project;

import static io.oneko.kubernetes.deployments.DesiredState.*;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.text.StringSubstitutor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Sets;

import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeployableConfigurationTemplates;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareListProperty;
import io.oneko.domain.ModificationAwareMapProperty;
import io.oneko.domain.ModificationAwareProperty;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.HasNamespace;
import io.oneko.namespace.ImplicitNamespace;
import io.oneko.namespace.Namespace;
import io.oneko.templates.ConfigurationTemplate;
import io.oneko.templates.ConfigurationTemplates;
import lombok.Builder;

/**
 * Each project has a number of versions. These versions are stored as part of the project they belong to.
 */
public class ProjectVersion extends ModificationAwareIdentifiable implements HasNamespace {

	private final ModificationAwareProperty<UUID> uuid = new ModificationAwareProperty<>(this, "uuid");
	private final Project project;
	private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private final ModificationAwareProperty<DeploymentBehaviour> deploymentBehaviour = new ModificationAwareProperty<>(this, "deploymentBehaviour");
	private final ModificationAwareProperty<Map<String, String>> templateVariables = new ModificationAwareMapProperty<>(this, "templateVariables");
	private final ModificationAwareProperty<String> dockerContentDigest = new ModificationAwareProperty<>(this, "dockerContentDigest");
	private final ModificationAwareProperty<List<String>> urls = new ModificationAwareListProperty<>(this, "urls");
	private final ModificationAwareProperty<List<ConfigurationTemplate>> configurationTemplates = new ModificationAwareListProperty<>(this, "configurationTemplates");
	private final ModificationAwareProperty<Boolean> outdated = new ModificationAwareProperty<>(this, "outdated");
	private final ModificationAwareProperty<LifetimeBehaviour> lifetimeBehaviour = new ModificationAwareProperty<>(this, "lifetimeBehaviour");
	private final ModificationAwareProperty<Namespace> namespace = new ModificationAwareProperty<>(this, "namespace");
	private final ModificationAwareProperty<DesiredState> desiredState = new ModificationAwareProperty<>(this, "desiredState");
	private final ModificationAwareProperty<Instant> imageUpdatedDate = new ModificationAwareProperty<>(this, "imageUpdatedDate");

	@Builder
	public ProjectVersion(UUID uuid, Project project, String name, DeploymentBehaviour deploymentBehaviour,
						  Map<String, String> templateVariables, String dockerContentDigest, List<String> urls,
						  List<ConfigurationTemplate> configurationTemplates, boolean outdated, LifetimeBehaviour lifetimeBehaviour,
						  DefinedNamespace namespace, DesiredState desiredState, Instant imageUpdatedDate) {
		this.uuid.init(uuid);
		this.project = project;
		this.name.init(name);
		this.deploymentBehaviour.init(deploymentBehaviour);
		this.dockerContentDigest.init(dockerContentDigest);
		this.templateVariables.init(templateVariables);
		this.urls.init(urls);
		this.outdated.init(outdated);
		this.configurationTemplates.init(configurationTemplates);
		this.lifetimeBehaviour.init(lifetimeBehaviour);
		this.namespace.init(Objects.requireNonNullElse(namespace, new ImplicitNamespace(this)));
		this.desiredState.init(Objects.requireNonNullElse(desiredState, NotDeployed));
		this.imageUpdatedDate.init(imageUpdatedDate);
	}

	/**
	 * Creates a new version of the given project.
	 */
	ProjectVersion(Project project, String name) {
		this.uuid.set(UUID.randomUUID());
		this.project = Objects.requireNonNull(project);
		this.name.set(name);
		this.outdated.set(false);
		this.deploymentBehaviour.set(project.getNewVersionsDeploymentBehaviour());
		this.namespace.set(new ImplicitNamespace(this));
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
	public Project getProject() {
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

	public Optional<LifetimeBehaviour> getEffectiveLifetimeBehaviour() {
		return getLifetimeBehaviour().or(project::getDefaultLifetimeBehaviour);
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

	public void setUrls(List<String> urls) {
		this.urls.set(urls);
	}

	public List<ConfigurationTemplate> getConfigurationTemplates() {
		return this.configurationTemplates.get();
	}

	public void setConfigurationTemplates(List<ConfigurationTemplate> configurationTemplates) {
		ConfigurationTemplates.ensureConsistentCollection(configurationTemplates);
		this.configurationTemplates.set(configurationTemplates);
	}

	public boolean isOutdated() {
		return this.outdated.get();
	}

	public void setOutdated(boolean outdated) {
		this.outdated.set(outdated);
	}

	/**
	 * A project version is orphaned if it's project has no docker registry assigned. This happens, when a registry is getting deleted.
	 * No deployments can be performed on orphaned project versions.
	 */
	public boolean isOrphan() {
		return this.project.isOrphan();
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

	public Map<String, String> getImplicitTemplateVariables() {
		Map<String, String> implicitTemplateVariables = new HashMap<>();
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.VERSION_NAME, this.getName());
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.ONEKO_VERSION, this.getId().toString());
		implicitTemplateVariables.put(ProjectConstants.TemplateVariablesNames.SAFE_VERSION_NAME, this.safeVersionName());

		return implicitTemplateVariables;
	}

	public String safeVersionName() {
		final String safeName = this.getName().replaceAll("-?([^-\\w]|_)*-?", "")
				.toLowerCase();
		return safeName.substring(0, Math.min(safeName.length(), 63));
	}

	/**
	 * Provides a mutable copy of all template variables retrieved by merging the ones from this version's
	 * {@link #getProject()} and this version's own {@link #getTemplateVariables()}.
	 *
	 * @return Never <code>null</code>
	 */
	public Map<String, String> calculateEffectiveTemplateVariables() {
		Map<String, String> mergedTemplateVariables = new HashMap<>();

		mergedTemplateVariables.putAll(this.getProject().getTemplateVariables().stream()
				.collect(Collectors.toMap(TemplateVariable::getName, TemplateVariable::getDefaultValue)));
		mergedTemplateVariables.putAll(this.getProject().getImplicitTemplateVariables());
		mergedTemplateVariables.putAll(this.getImplicitTemplateVariables());
		mergedTemplateVariables.putAll(this.getTemplateVariables());

		return mergedTemplateVariables;
	}

	/**
	 * Provides the effective template to use on this version. This is either derived from the project's configuration
	 * template or a modified version template and the effective template variables.
	 */
	public String calculateConfiguration() {
		return getCalculatedConfigurationTemplates().stream()
				.map(this::templateAsYAMLString)
				.collect(Collectors.joining("\n\n------\n\n"));
	}

	/**
	 * Provides all effective templates to use on this version. This is either derived from the project's configuration
	 * template or a modified version template and the effective template variables.
	 */
	public List<ConfigurationTemplate> getCalculatedConfigurationTemplates() {
		StringSubstitutor sub = new StringSubstitutor(this.calculateEffectiveTemplateVariables());

		return ConfigurationTemplates.unifyTemplateSets(project.getDefaultConfigurationTemplates(), getConfigurationTemplates()).stream()
				.map(ConfigurationTemplate::clone)
				.peek(template -> template.setContent(sub.replace(template.getContent())))
				.collect(Collectors.toList());
	}

	public DeployableConfigurationTemplates calculateDeployableConfigurationTemplates() {
		return DeployableConfigurationTemplates.of(this.getCalculatedConfigurationTemplates());
	}

	public Namespace getNamespace() {
		return namespace.get();
	}

	/**
	 * Provides the ID of the defined namespace (if one is set.)
	 *
	 * @return might be <code>null</code>
	 */
	public UUID getDefinedNamespaceId() {
		return Optional.of(this.getNamespace())
				.filter(DefinedNamespace.class::isInstance)
				.map(namespace -> ((DefinedNamespace) namespace).getId())
				.orElse(null);
	}

	public void assignDefinedNamespace(DefinedNamespace namespace) {
		this.namespace.set(namespace);
	}

	public void resetToImplicitNamespace() {
		if (this.getNamespace() instanceof ImplicitNamespace) {
			return;
		}
		this.namespace.set(new ImplicitNamespace(this));
	}

	@Override
	public String getProtoNamespace() {
		return getProject().getName() + "-" + getName();
	}

	private String templateAsYAMLString(ConfigurationTemplate configurationTemplate) {
		return "# > " +
				configurationTemplate.getName() +
				" (" +
				configurationTemplate.getDescription() +
				")\n\n" +
				configurationTemplate.getContent();
	}

	@Override
	public Set<String> getDirtyProperties() {
		Set<String> dirtyProperties = super.getDirtyProperties();
		if (this.getConfigurationTemplates().stream().anyMatch(ConfigurationTemplate::isDirty)) {
			dirtyProperties = Sets.union(dirtyProperties, Collections.singleton("configurationTemplates"));
		}
		return dirtyProperties;
	}

	@Override
	public Map<String, String> getNamespaceLabels() {
		Map<String, String> labels = new HashMap<>();
		labels.put(ProjectConstants.TemplateVariablesNames.ONEKO_VERSION, this.getId().toString());
		labels.put(ProjectConstants.TemplateVariablesNames.ONEKO_PROJECT, getProject().getId().toString());
		labels.put("name", this.getNamespace().asKubernetesNameSpace());
		return labels;
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

}
