package io.oneko.docker.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import io.oneko.Profiles;

@Profile(Profiles.MONGO)
interface DockerRegistryMongoSpringRepository extends MongoRepository<DockerRegistryMongo, UUID> {
	Optional<DockerRegistryMongo> findByName(String name);
}
