package io.oneko.projectmesh;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;

import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.HasNamespace;
import io.oneko.namespace.Namespace;
import io.oneko.project.ProjectConstants;

public interface ProjectMesh<M extends ProjectMesh<M, C>, C extends MeshComponent<M, C>> extends HasNamespace {

	UUID getId();

	String getName();

	Namespace getNamespace();

	/**
	 * Provides the ID of the defined namespace (if one is set.)
	 *
	 * @return might be <code>null</code>
	 */
	default UUID getDefinedNamespaceId() {
		return Optional.of(this.getNamespace())
				.filter(DefinedNamespace.class::isInstance)
				.map(namespace -> ((DefinedNamespace) namespace).getId())
				.orElse(null);
	}

	@Override
	default String getProtoNamespace() {
		return this.getId().toString();
	}

	Optional<LifetimeBehaviour> getLifetimeBehaviour();

	DeploymentBehaviour getDeploymentBehaviour();

	ImmutableList<C> getComponents();

	default Optional<C> getComponentById(UUID componentId) {
		return getComponents().stream()
				.filter(c -> c.getId().equals(componentId))
				.findAny();
	}

	default Optional<C> getComponentByName(String name) {
		return getComponents().stream()
				.filter(c -> StringUtils.equals(c.getName(), name))
				.findFirst();
	}

	default boolean hasComponent(String name) {
		return getComponentByName(name).isPresent();
	}

	@Override
	default Map<String, String> getNamespaceLabels() {
		Map<String, String> labels = new HashMap<>();
		labels.put(ProjectConstants.TemplateVariablesNames.ONEKO_MESH, this.getId().toString());
		labels.put("name", this.getNamespace().asKubernetesNameSpace());
		return labels;
	}
}
