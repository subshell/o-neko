package io.oneko.docker.persistence;

import io.oneko.Profiles;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

@Profile(Profiles.MONGO)
interface DockerRegistryMongoSpringRepository extends MongoRepository<DockerRegistryMongo, UUID> {
	Optional<DockerRegistryMongo> findByName(String name);
}
