package io.oneko.kubernetes.deployments;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface Deployment {

    UUID getId();

    UUID getDeployableId();

    DeployableStatus getStatus();

    Optional<Instant> getTimestamp();

    int getContainerCount();

    int getReadyContainerCount();
}
