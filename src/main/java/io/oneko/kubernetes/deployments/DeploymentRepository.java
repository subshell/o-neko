package io.oneko.kubernetes.deployments;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeploymentRepository {

	Mono<Deployment> findByDeployableId(UUID projectVersionId);

	Mono<Deployment> save(Deployment entity);

	Mono<Void> deleteById(UUID uuid);

	Mono<Deployment> findById(UUID uuid);

	Flux<Deployment> findAll();

	Flux<Deployment> findAllById(Iterable<UUID> uuids);

	Flux<Deployment> findAllByDeployableIdIn(Iterable<UUID> uuids);
}
