package io.oneko.namespace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Pretty much a persistent collection of defined namespaces
 */
public interface NamespaceRepository {

	Optional<ReadableNamespace> getById(UUID id);

	Optional<ReadableNamespace> getByName(String name);

	List<ReadableNamespace> getAll();

	ReadableNamespace add(WritableNamespace namespace);

	void remove(Namespace namespace);
}
