package io.oneko.kubernetes.deployments;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeploymentRepository {

	Optional<ReadableDeployment> findByProjectVersionId(UUID projectVersionId);

	ReadableDeployment save(WritableDeployment entity);

	void deleteById(UUID uuid);

	Optional<ReadableDeployment> findById(UUID uuid);

	List<ReadableDeployment> findAll();

	List<ReadableDeployment> findAllById(Iterable<UUID> uuids);

	List<ReadableDeployment> findAllByProjectVersionIdIn(Iterable<UUID> uuids);
}
