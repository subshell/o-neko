package io.oneko.kubernetes.deployments.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import io.oneko.kubernetes.deployments.Deployment;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeploymentMongoRepository implements DeploymentRepository {

	private final DeploymentMongoSpringRepository innerRepository;

	public DeploymentMongoRepository(DeploymentMongoSpringRepository innerRepository) {
		this.innerRepository = innerRepository;
	}

	@Override
	public Optional<Deployment> findByDeployableId(UUID deployableId) {
		return innerRepository.findByDeployableId(deployableId)
				.map(this::fromDeploymentMongo);
	}

	@Override
	public Deployment save(Deployment entity) {
		if (entity.isDirty()) {
			return fromDeploymentMongo(innerRepository.save(toDeploymentMongo(entity)));
		} else {
			return entity;
		}
	}

	@Override
	public void deleteById(UUID uuid) {
		innerRepository.deleteById(uuid);
	}

	@Override
	public Optional<Deployment> findById(UUID uuid) {
		return innerRepository.findById(uuid).map(this::fromDeploymentMongo);
	}

	@Override
	public List<Deployment> findAll() {
		return innerRepository.findAll().stream().map(this::fromDeploymentMongo).collect(Collectors.toList());
	}

	@Override
	public List<Deployment> findAllById(Iterable<UUID> uuids) {
		return StreamSupport.stream(innerRepository.findAllById(uuids).spliterator(), false).map(this::fromDeploymentMongo)
				.collect(Collectors.toList());
	}

	@Override
	public List<Deployment> findAllByDeployableIdIn(Iterable<UUID> uuids) {
		return innerRepository.findAllByDeployableIdIn(uuids).stream().map(this::fromDeploymentMongo).collect(Collectors.toList());
	}

	private Deployment fromDeploymentMongo(DeploymentMongo mongo) {
		return Deployment.builder()
				.id(mongo.getId())
				.deployableId(mongo.getDeployableId())
				.status(mongo.getStatus())
				.timestamp(mongo.getTimestamp())
				.containerCount(mongo.getContainerCount())
				.readyContainerCount(mongo.getReadyContainerCount())
				.build();
	}

	private DeploymentMongo toDeploymentMongo(Deployment deployment) {
		return DeploymentMongo.builder()
				.id(deployment.getId())
				.deployableId(deployment.getDeployableId())
				.status(deployment.getStatus())
				.timestamp(deployment.getTimestamp().orElse(null))
				.containerCount(deployment.getContainerCount())
				.readyContainerCount(deployment.getReadyContainerCount())
				.build();
	}
}
