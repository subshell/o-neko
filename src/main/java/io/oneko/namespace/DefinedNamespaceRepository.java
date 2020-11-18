package io.oneko.namespace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Pretty much a persistent collection of defined namespaces
 */
public interface DefinedNamespaceRepository {

	Optional<ReadableDefinedNamespace> getById(UUID id);

	Optional<ReadableDefinedNamespace> getByName(String name);

	List<ReadableDefinedNamespace> getAll();

	ReadableDefinedNamespace add(WritableDefinedNamespace namespace);

	void remove(DefinedNamespace namespace);
}
