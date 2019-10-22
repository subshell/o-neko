package io.oneko.projectmesh.persistence;

import java.util.UUID;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

public interface ProjectMeshMongoSpringRepository extends ReactiveMongoRepository<ProjectMeshMongo, UUID> {
	Mono<ProjectMeshMongo> findByName(String name);
}
