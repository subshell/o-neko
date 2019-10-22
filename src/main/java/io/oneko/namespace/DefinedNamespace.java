package io.oneko.namespace;

import java.util.UUID;

import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareProperty;
import lombok.Builder;

/**
 * This is an explicitly defined namespace that can be used for project versions.
 */
public class DefinedNamespace extends ModificationAwareIdentifiable implements Namespace {

	private final ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
	private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");

	/**
	 * Creates a new namespace from scratch.
	 *
	 * @param name - must not be <code>null</code>
	 */
	public DefinedNamespace(String name) {
		this.id.set(UUID.randomUUID());
		this.name.set(NamespaceConventions.sanitizeNamespace(name));
	}

	/**
	 * Creates a namespace instance for a really existing namespace.
	 *
	 * @param id   - must not be <code>null</code>
	 * @param name - must not be <code>null</code>
	 */
	@Builder
	public DefinedNamespace(UUID id, String name) {
		this.id.init(id);
		this.name.init(NamespaceConventions.sanitizeNamespace(name));
	}

	@Override
	public UUID getId() {
		return id.get();
	}

	@Override
	public String asKubernetesNameSpace() {
		return name.get();
	}
}
