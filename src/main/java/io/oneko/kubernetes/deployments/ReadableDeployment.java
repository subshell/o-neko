package io.oneko.kubernetes.deployments;

import io.oneko.domain.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ReadableDeployment extends Identifiable implements Deployment {

    private final UUID id;
    private final UUID projectVersionId;
    private final DeployableStatus status;
    private final Instant timestamp;
    private final List<String> releaseNames;

    public WritableDeployment writable() {
        return WritableDeployment.builder()
                .id(id)
                .projectVersionId(projectVersionId)
                .status(status)
                .timestamp(timestamp)
                .releaseNames(releaseNames)
                .build();
    }

    @Override
    public Optional<Instant> getTimestamp() {
        return Optional.ofNullable(timestamp);
    }
}
