package io.oneko.project.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import io.oneko.Profiles;

@Profile(Profiles.MONGO)
interface ProjectMongoSpringRepository extends MongoRepository<ProjectMongo, UUID> {
	Optional<ProjectMongo> findByName(String name);

	List<ProjectMongo> findByDockerRegistryUUID(UUID dockerRegistryUUID);
}
