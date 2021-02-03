package io.oneko.kubernetes.deployments.persistence;

import com.google.common.collect.ImmutableList;
import io.oneko.Profiles;
import io.oneko.kubernetes.deployments.ReadableDeployment;
import io.oneko.kubernetes.deployments.WritableDeployment;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Profile(Profiles.IN_MEMORY)
public class DeploymentInMemoryRepository implements DeploymentRepository {

    Map<UUID, ReadableDeployment> deployments = new HashMap<>();

    @Override
    public Optional<ReadableDeployment> findByProjectVersionId(UUID projectVersionId) {
        return deployments.values().stream()
                .filter(deployment -> deployment.getProjectVersionId().equals(projectVersionId))
                .findFirst();
    }

    @Override
    public ReadableDeployment save(WritableDeployment entity) {
        final ReadableDeployment readable = entity.readable();
        deployments.put(readable.getId(), readable);
        return readable;
    }

    @Override
    public void deleteById(UUID uuid) {
        deployments.remove(uuid);
    }

    @Override
    public Optional<ReadableDeployment> findById(UUID uuid) {
        return Optional.ofNullable(deployments.get(uuid));
    }

    @Override
    public List<ReadableDeployment> findAll() {
        return ImmutableList.copyOf(deployments.values());
    }

    @Override
    public List<ReadableDeployment> findAllById(Iterable<UUID> uuids) {
        return StreamSupport.stream(uuids.spliterator(), false)
                .map(uuid -> deployments.get(uuid))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReadableDeployment> findAllByProjectVersionIdIn(Iterable<UUID> uuids) {
        Set<UUID> uuidsAsSet = new HashSet<>();
        uuids.forEach(uuidsAsSet::add);
        return deployments.values().stream()
                .filter(deployment -> uuidsAsSet.contains(deployment.getProjectVersionId()))
                .collect(Collectors.toList());
    }
}
