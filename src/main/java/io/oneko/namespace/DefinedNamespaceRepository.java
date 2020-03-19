package io.oneko.namespace;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Pretty much a persistent collection of defined namespaces
 */
public interface DefinedNamespaceRepository {

	Mono<ReadableDefinedNamespace> getById(UUID id);

	Mono<ReadableDefinedNamespace> getByName(String name);

	Flux<ReadableDefinedNamespace> getAll();

	Mono<ReadableDefinedNamespace> add(WritableDefinedNamespace namespace);

	Mono<Void> remove(DefinedNamespace namespace);
}
