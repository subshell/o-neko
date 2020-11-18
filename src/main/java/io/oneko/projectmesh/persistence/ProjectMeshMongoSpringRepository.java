package io.oneko.projectmesh.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectMeshMongoSpringRepository extends MongoRepository<ProjectMeshMongo, UUID> {
	Optional<ProjectMeshMongo> findByName(String name);
}
