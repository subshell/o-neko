package io.oneko.docker.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

interface DockerRegistryMongoSpringRepository extends MongoRepository<DockerRegistryMongo, UUID> {
	Optional<DockerRegistryMongo> findByName(String name);
}
