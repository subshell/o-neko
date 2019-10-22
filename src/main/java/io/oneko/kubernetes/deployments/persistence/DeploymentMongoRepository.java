package io.oneko.kubernetes.deployments.persistence;

import java.util.UUID;

import org.springframework.stereotype.Service;

import io.oneko.kubernetes.deployments.Deployment;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DeploymentMongoRepository implements DeploymentRepository {

	private final DeploymentMongoSpringRepository innerRepository;

	public DeploymentMongoRepository(DeploymentMongoSpringRepository innerRepository) {
		this.innerRepository = innerRepository;
	}

	@Override
	public Mono<Deployment> findByDeployableId(UUID deployableId) {
		return innerRepository.findByDeployableId(deployableId)
				.map(this::fromDeploymentMongo);
	}

	@Override
	public Mono<Deployment> save(Deployment entity) {
		if (entity.isDirty()) {
			return innerRepository.save(toDeploymentMongo(entity)).map(this::fromDeploymentMongo);
		} else {
			return Mono.just(entity);
		}
	}

	@Override
	public Mono<Void> deleteById(UUID uuid) {
		return innerRepository.deleteById(uuid);
	}

	@Override
	public Mono<Deployment> findById(UUID uuid) {
		return innerRepository.findById(uuid).map(this::fromDeploymentMongo);
	}

	@Override
	public Flux<Deployment> findAll() {
		return innerRepository.findAll().map(this::fromDeploymentMongo);
	}

	@Override
	public Flux<Deployment> findAllById(Iterable<UUID> uuids) {
		return innerRepository.findAllById(uuids).map(this::fromDeploymentMongo);
	}

	@Override
	public Flux<Deployment> findAllByDeployableIdIn(Iterable<UUID> uuids) {
		return innerRepository.findAllByDeployableIdIn(uuids).map(this::fromDeploymentMongo);
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
