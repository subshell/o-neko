package io.oneko.kubernetes.deployments;

import io.oneko.domain.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ReadableDeployment extends Identifiable implements Deployment {

    private final UUID id;
    private final UUID deployableId;
    private final DeployableStatus status;
    private final Instant timestamp;

    public WritableDeployment writable() {
        return WritableDeployment.builder()
                .id(id)
                .deployableId(deployableId)
                .status(status)
                .timestamp(timestamp)
                .build();
    }

    @Override
    public Optional<Instant> getTimestamp() {
        return Optional.ofNullable(timestamp);
    }
}
