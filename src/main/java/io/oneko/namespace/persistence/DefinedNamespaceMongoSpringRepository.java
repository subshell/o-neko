package io.oneko.namespace.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DefinedNamespaceMongoSpringRepository extends MongoRepository<DefinedNamespaceMongo, UUID> {
	Optional<DefinedNamespaceMongo> findByName(String name);
}
