package io.oneko.namespace.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface DefinedNamespaceMongoSpringRepository extends MongoRepository<DefinedNamespaceMongo, UUID> {
	Optional<DefinedNamespaceMongo> findByName(String name);
}
