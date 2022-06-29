package io.oneko.kubernetes.deployments.persistence;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import io.oneko.kubernetes.deployments.ReadableDeployment;
import io.oneko.kubernetes.deployments.WritableDeployment;
import io.oneko.project.ProjectVersionLock;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile(Profiles.MONGO)
@AllArgsConstructor
public class DeploymentMongoRepository implements DeploymentRepository {

	private final DeploymentMongoSpringRepository innerRepository;
	private final ProjectVersionLock projectVersionLock;

	@Override
	public Optional<ReadableDeployment> findByProjectVersionId(UUID projectVersionId) {
		return innerRepository.findByProjectVersionId(projectVersionId)
				.map(this::fromDeploymentMongo);
	}

	@Override
	public ReadableDeployment save(WritableDeployment entity) {
		if (!projectVersionLock.currentThreadHasLock(entity.getProjectVersionId())) {
			throw new ConcurrentModificationException("Current thread doesn't own the lock to edit the deployment.");
		}

		if (entity.isDirty()) {
			return fromDeploymentMongo(innerRepository.save(toDeploymentMongo(entity)));
		} else {
			return entity.readable();
		}
	}

	@Override
	public void deleteById(UUID uuid) {
		innerRepository.deleteById(uuid);
	}

	@Override
	public Optional<ReadableDeployment> findById(UUID uuid) {
		return innerRepository.findById(uuid).map(this::fromDeploymentMongo);
	}

	@Override
	public List<ReadableDeployment> findAll() {
		return innerRepository.findAll().stream().map(this::fromDeploymentMongo).collect(Collectors.toList());
	}

	@Override
	public List<ReadableDeployment> findAllById(Iterable<UUID> uuids) {
		return StreamSupport.stream(innerRepository.findAllById(uuids).spliterator(), false).map(this::fromDeploymentMongo)
				.collect(Collectors.toList());
	}

	@Override
	public List<ReadableDeployment> findAllByProjectVersionIdIn(Iterable<UUID> uuids) {
		return innerRepository.findAllByProjectVersionIdIn(uuids).stream().map(this::fromDeploymentMongo).collect(Collectors.toList());
	}

	private ReadableDeployment fromDeploymentMongo(DeploymentMongo mongo) {
		return ReadableDeployment.builder()
				.id(mongo.getId())
				.projectVersionId(mongo.getProjectVersionId())
				.status(mongo.getStatus())
				.timestamp(mongo.getTimestamp())
				.releaseNames(mongo.getReleaseNames())
				.build();
	}

	private DeploymentMongo toDeploymentMongo(WritableDeployment deployment) {
		return DeploymentMongo.builder()
				.id(deployment.getId())
				.projectVersionId(deployment.getProjectVersionId())
				.status(deployment.getStatus())
				.timestamp(deployment.getTimestamp().orElse(null))
				.releaseNames(deployment.getReleaseNames())
				.build();
	}
}
