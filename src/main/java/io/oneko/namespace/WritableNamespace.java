package io.oneko.namespace;

import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareProperty;
import lombok.Builder;

import java.util.UUID;

public class WritableNamespace extends ModificationAwareIdentifiable implements Namespace {

	private final ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
	private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");

	/**
	 * Creates a new namespace from scratch.
	 *
	 * @param name - must not be <code>null</code>
	 */
	public WritableNamespace(String name) {
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
	public WritableNamespace(UUID id, String name) {
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

	public ReadableNamespace readable() {
		return new ReadableNamespace(getId(), asKubernetesNameSpace());
	}

}
