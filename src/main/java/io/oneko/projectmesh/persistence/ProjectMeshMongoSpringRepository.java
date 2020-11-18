package io.oneko.projectmesh.persistence;

import io.oneko.Profiles;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

@Profile(Profiles.MONGO)
public interface ProjectMeshMongoSpringRepository extends MongoRepository<ProjectMeshMongo, UUID> {
	Optional<ProjectMeshMongo> findByName(String name);
}
