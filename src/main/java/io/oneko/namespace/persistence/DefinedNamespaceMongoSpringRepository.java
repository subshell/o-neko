package io.oneko.namespace.persistence;

import java.util.UUID;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

public interface DefinedNamespaceMongoSpringRepository extends ReactiveMongoRepository<DefinedNamespaceMongo, UUID> {
	Mono<DefinedNamespaceMongo> findByName(String name);
}
