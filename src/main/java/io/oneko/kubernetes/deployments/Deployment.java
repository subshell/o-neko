package io.oneko.kubernetes.deployments;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface Deployment {

    UUID getId();

    UUID getProjectVersionId();

    DeployableStatus getStatus();

    Optional<Instant> getTimestamp();
}
