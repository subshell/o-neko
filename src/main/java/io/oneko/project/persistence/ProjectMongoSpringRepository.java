package io.oneko.project.persistence;

import io.oneko.Profiles;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

@Profile(Profiles.MONGO)
interface ProjectMongoSpringRepository extends MongoRepository<ProjectMongo, UUID> {
	Optional<ProjectMongo> findByName(String name);

	List<ProjectMongo> findByDockerRegistryUUID(UUID dockerRegistryUUID);

	List<ProjectMongo> queryByNameContainingIgnoreCase(String search);

	List<ProjectMongo> queryByVersions_Name_ContainingIgnoreCase(String search);
}
