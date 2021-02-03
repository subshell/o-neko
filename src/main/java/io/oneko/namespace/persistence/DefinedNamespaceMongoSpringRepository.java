package io.oneko.namespace.persistence;

import io.oneko.Profiles;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

@Profile(Profiles.MONGO)
public interface DefinedNamespaceMongoSpringRepository extends MongoRepository<NamespaceMongo, UUID> {
	Optional<NamespaceMongo> findByName(String name);
}
