package io.oneko.project.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

interface ProjectMongoSpringRepository extends MongoRepository<ProjectMongo, UUID> {
	Optional<ProjectMongo> findByName(String name);

	List<ProjectMongo> findByDockerRegistryUUID(UUID dockerRegistryUUID);
}
