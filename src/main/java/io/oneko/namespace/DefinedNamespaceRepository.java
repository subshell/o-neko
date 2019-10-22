package io.oneko.namespace;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Pretty much a persistent collection of defined namespaces
 */
public interface DefinedNamespaceRepository {

	Mono<DefinedNamespace> getById(UUID id);

	Mono<DefinedNamespace> getByName(String name);

	Flux<DefinedNamespace> getAll();

	Mono<DefinedNamespace> add(DefinedNamespace namespace);

	Mono<Void> remove(DefinedNamespace namespace);
}
