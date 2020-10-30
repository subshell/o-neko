package io.oneko.kubernetes.deployments.persistence;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeploymentMongoSpringRepository extends MongoRepository<DeploymentMongo, UUID> {

	Optional<DeploymentMongo> findByDeployableId(UUID deployableId);

	List<DeploymentMongo> findAllByDeployableIdIn(Iterable<UUID> projectVersionIds);

}
