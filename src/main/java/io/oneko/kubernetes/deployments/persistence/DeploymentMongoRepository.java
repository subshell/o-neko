package io.oneko.kubernetes.deployments.persistence;

import io.oneko.Profiles;
import io.oneko.kubernetes.deployments.ReadableDeployment;
import io.oneko.kubernetes.deployments.WritableDeployment;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@Profile(Profiles.MONGO)
public class DeploymentMongoRepository implements DeploymentRepository {

	private final DeploymentMongoSpringRepository innerRepository;

	public DeploymentMongoRepository(DeploymentMongoSpringRepository innerRepository) {
		this.innerRepository = innerRepository;
	}

	@Override
	public Optional<ReadableDeployment> findByDeployableId(UUID deployableId) {
		return innerRepository.findByDeployableId(deployableId)
				.map(this::fromDeploymentMongo);
	}

	@Override
	public ReadableDeployment save(WritableDeployment entity) {
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
	public List<ReadableDeployment> findAllByDeployableIdIn(Iterable<UUID> uuids) {
		return innerRepository.findAllByDeployableIdIn(uuids).stream().map(this::fromDeploymentMongo).collect(Collectors.toList());
	}

	private ReadableDeployment fromDeploymentMongo(DeploymentMongo mongo) {
		return ReadableDeployment.builder()
				.id(mongo.getId())
				.deployableId(mongo.getProjectVersionId())
				.status(mongo.getStatus())
				.timestamp(mongo.getTimestamp())
				.build();
	}

	private DeploymentMongo toDeploymentMongo(WritableDeployment deployment) {
		return DeploymentMongo.builder()
				.id(deployment.getId())
				.projectVersionId(deployment.getDeployableId())
				.status(deployment.getStatus())
				.timestamp(deployment.getTimestamp().orElse(null))
				.build();
	}
}
