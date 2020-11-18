package io.oneko.docker.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

interface DockerRegistryMongoSpringRepository extends MongoRepository<DockerRegistryMongo, UUID> {
	Optional<DockerRegistryMongo> findByName(String name);
}
