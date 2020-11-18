package io.oneko.projectmesh;

import com.google.common.collect.ImmutableList;
import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.Namespace;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class ReadableProjectMesh implements ProjectMesh<ReadableProjectMesh, ReadableMeshComponent> {

	private final UUID id;
	private final String name;
	private final Namespace namespace;
	private final DeploymentBehaviour deploymentBehaviour;
	private final LifetimeBehaviour lifetimeBehaviour;
	private final ImmutableList<ReadableMeshComponent> components;

	@Builder
	public ReadableProjectMesh(UUID id, String name, Namespace namespace, DeploymentBehaviour deploymentBehaviour, LifetimeBehaviour lifetimeBehaviour, List<ReadableMeshComponent> components) {
		this.id = id;
		this.name = name;
		this.namespace = namespace;
		this.deploymentBehaviour = deploymentBehaviour;
		this.lifetimeBehaviour = lifetimeBehaviour;
		this.components = ImmutableList.copyOf(components);
		components.forEach(c -> c.setOwner(this));
	}

	public Optional<LifetimeBehaviour> getLifetimeBehaviour() {
		return Optional.ofNullable(lifetimeBehaviour);
	}

	public WritableProjectMesh writable() {
		final List<WritableMeshComponent> components = getComponents().stream()
				.map(ReadableMeshComponent::writable)
				.collect(Collectors.toList());
		return WritableProjectMesh.builder()
				.id(getId())
				.name(getName())
				.namespace(getNamespace() instanceof DefinedNamespace ? (DefinedNamespace)getNamespace() : null)
				.deploymentBehaviour(getDeploymentBehaviour())
				.lifetimeBehaviour(getLifetimeBehaviour().orElse(null))
				.components(components)
				.build();
	}
}
