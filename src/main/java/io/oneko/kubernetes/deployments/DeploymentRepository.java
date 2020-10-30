package io.oneko.kubernetes.deployments;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeploymentRepository {

	Optional<Deployment> findByDeployableId(UUID projectVersionId);

	Deployment save(Deployment entity);

	void deleteById(UUID uuid);

	Optional<Deployment> findById(UUID uuid);

	List<Deployment> findAll();

	List<Deployment> findAllById(Iterable<UUID> uuids);

	List<Deployment> findAllByDeployableIdIn(Iterable<UUID> uuids);
}
