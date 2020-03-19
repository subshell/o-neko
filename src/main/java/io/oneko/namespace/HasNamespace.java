package io.oneko.namespace;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.oneko.domain.Identifiable;

public interface HasNamespace {

	/**
	 * Same signature as {@link Identifiable#getId()} by intention.
	 */
	UUID getId();

	/**
	 * Provides the raw string from which the sanitized namespace is generated.
	 */
	String getProtoNamespace();

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

	Map<String, String> getNamespaceLabels();
}
