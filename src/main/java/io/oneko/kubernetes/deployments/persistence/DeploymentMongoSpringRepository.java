package io.oneko.kubernetes.deployments.persistence;


import io.oneko.Profiles;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Profile(Profiles.MONGO)
public interface DeploymentMongoSpringRepository extends MongoRepository<DeploymentMongo, UUID> {

	Optional<DeploymentMongo> findByDeployableId(UUID deployableId);

	List<DeploymentMongo> findAllByDeployableIdIn(Iterable<UUID> projectVersionIds);

}
