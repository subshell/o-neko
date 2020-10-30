package io.oneko.projectmesh.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectMeshMongoSpringRepository extends MongoRepository<ProjectMeshMongo, UUID> {
	Optional<ProjectMeshMongo> findByName(String name);
}
