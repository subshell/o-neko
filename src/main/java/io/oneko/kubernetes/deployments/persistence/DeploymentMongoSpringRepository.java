package io.oneko.kubernetes.deployments.persistence;


import java.util.UUID;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeploymentMongoSpringRepository extends ReactiveMongoRepository<DeploymentMongo, UUID> {

	Mono<DeploymentMongo> findByDeployableId(UUID deployableId);

	Flux<DeploymentMongo> findAllByDeployableIdIn(Iterable<UUID> projectVersionIds);

}
