package io.oneko.projectmesh;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareProperty;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.ImplicitNamespace;
import io.oneko.namespace.Namespace;
import io.oneko.namespace.WritableHasNamespace;
import io.oneko.project.ReadableProject;
import io.oneko.project.ReadableProjectVersion;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class WritableProjectMesh extends ModificationAwareIdentifiable implements ProjectMesh<WritableProjectMesh, WritableMeshComponent>, WritableHasNamespace {

	private final ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
	private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private final ModificationAwareProperty<Namespace> namespace = new ModificationAwareProperty<>(this, "namespace");
	private final ModificationAwareProperty<DeploymentBehaviour> deploymentBehaviour = new ModificationAwareProperty<>(this, "deploymentBehaviour");
	private final ModificationAwareProperty<LifetimeBehaviour> lifetimeBehaviour = new ModificationAwareProperty<>(this, "lifetimeBehaviour");
	private final List<WritableMeshComponent> components;

	public WritableProjectMesh() {
		this.id.set(UUID.randomUUID());
		this.namespace.set(new ImplicitNamespace(this));
		this.lifetimeBehaviour.set(LifetimeBehaviour.ofDays(3));
		this.components = new ArrayList<>();
	}

	@Builder
	public WritableProjectMesh(UUID id, String name, DefinedNamespace namespace, DeploymentBehaviour deploymentBehaviour, LifetimeBehaviour lifetimeBehaviour, List<WritableMeshComponent> components) {
		this.id.init(id);
		this.name.init(name);
		this.namespace.init(Objects.requireNonNullElse(namespace, new ImplicitNamespace(this)));
		this.deploymentBehaviour.init(deploymentBehaviour);
		this.lifetimeBehaviour.init(lifetimeBehaviour);
		this.components = new ArrayList<>(components);
		components.forEach(c -> c.setOwner(this));
	}

	@Override
	public UUID getId() {
		return this.id.get();
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
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

	public ImplicitNamespace resetToImplicitNamespace() {
		if (this.getNamespace() instanceof ImplicitNamespace) {
			return (ImplicitNamespace) this.getNamespace();
		}
		final var implicitNamespace = new ImplicitNamespace(this);
		this.namespace.set(implicitNamespace);

		return implicitNamespace;
	}

	@Override
	public String getProtoNamespace() {
		return this.id.get().toString();
	}

	public Optional<LifetimeBehaviour> getLifetimeBehaviour() {
		return Optional.ofNullable(this.lifetimeBehaviour.get());
	}

	public void setLifetimeBehaviour(LifetimeBehaviour lifetimeBehaviour) {
		this.lifetimeBehaviour.set(lifetimeBehaviour);
	}

	public DeploymentBehaviour getDeploymentBehaviour() {
		return deploymentBehaviour.get();
	}

	public void setDeploymentBehaviour(DeploymentBehaviour deploymentBehaviour) {
		this.deploymentBehaviour.set(deploymentBehaviour);
	}

	public ImmutableList<WritableMeshComponent> getComponents() {
		return ImmutableList.copyOf(this.components);
	}

	public Optional<WritableMeshComponent> getComponentById(UUID componentId) {
		return this.components.stream()
				.filter(c -> c.getId().equals(componentId))
				.findAny();
	}

	public Optional<WritableMeshComponent> getComponentByName(String name) {
		return components.stream()
				.filter(c -> StringUtils.equals(c.getName(), name))
				.findFirst();
	}

	/**
	 * Adds a new project version to this project.
	 */
	public WritableMeshComponent createComponent(String name, ReadableProject project, ReadableProjectVersion projectVersion) {
		Preconditions.checkArgument(projectVersion.getProject() == project, "Version must belong to project");
		final Optional<WritableMeshComponent> componentByName = getComponentByName(name);
		if (componentByName.isPresent()) {
			Preconditions.checkArgument(componentByName.get().getProjectVersion() == projectVersion, "A component with this name is yet existing but refers to another project version.");
			return componentByName.get();
		}

		WritableMeshComponent component = new WritableMeshComponent(this, project, projectVersion);
		component.setName(name);
		this.components.add(component);
		return component;
	}

	/**
	 * Removes a project version from this project.
	 *
	 * @return the removed version - might be <code>null</code>
	 */
	public WritableMeshComponent removeComponent(String name) {
		for (int i = 0; i < components.size(); i++) {
			if (StringUtils.equals(components.get(i).getName(), name)) {
				this.touchProperty("components");
				return components.remove(i);
			}
		}
		return null;
	}

	public boolean hasComponent(String name) {
		return getComponentByName(name).isPresent();
	}

	@Override
	public Set<String> getDirtyProperties() {
		Set<String> dirtyProperties = super.getDirtyProperties();
		if (this.components.stream().anyMatch(WritableMeshComponent::isDirty)) {
			dirtyProperties = Sets.union(dirtyProperties, Collections.singleton("components"));
		}
		return dirtyProperties;
	}

	public ReadableProjectMesh readable() {
		final List<ReadableMeshComponent> components = getComponents().stream()
				.map(WritableMeshComponent::readable)
				.collect(Collectors.toList());
		return ReadableProjectMesh.builder()
				.id(getId())
				.name(getName())
				.namespace(getNamespace() instanceof DefinedNamespace ? (DefinedNamespace)getNamespace() : null)
				.deploymentBehaviour(getDeploymentBehaviour())
				.lifetimeBehaviour(getLifetimeBehaviour().orElse(null))
				.components(components)
				.build();
	}

}
