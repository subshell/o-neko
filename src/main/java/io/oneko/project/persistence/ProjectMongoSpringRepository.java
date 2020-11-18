package io.oneko.project.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ProjectMongoSpringRepository extends MongoRepository<ProjectMongo, UUID> {
	Optional<ProjectMongo> findByName(String name);

	List<ProjectMongo> findByDockerRegistryUUID(UUID dockerRegistryUUID);
}
