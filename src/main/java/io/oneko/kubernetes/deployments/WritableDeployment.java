package io.oneko.kubernetes.deployments;

import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareProperty;
import lombok.Builder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class WritableDeployment extends ModificationAwareIdentifiable implements Deployment {

	private ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
	private ModificationAwareProperty<UUID> projectVersionId = new ModificationAwareProperty<>(this, "projectVersionId");
	private ModificationAwareProperty<DeployableStatus> status = new ModificationAwareProperty<>(this, "status");
	private ModificationAwareProperty<Instant> timestamp = new ModificationAwareProperty<>(this, "timestamp");

	public WritableDeployment(UUID projectVersionID, DeployableStatus status, Instant timestamp, int containerCount, int readyContainerCount) {
		this.id.set(UUID.randomUUID());
		this.projectVersionId.set(projectVersionID);
		this.status.set(status);
		this.timestamp.set(timestamp);
	}

	@Builder
	public WritableDeployment(UUID id, UUID projectVersionId, DeployableStatus status, Instant timestamp, int containerCount, int readyContainerCount) {
		this.id.init(id);
		this.projectVersionId.init(projectVersionId);
		this.status.init(status);
		this.timestamp.init(timestamp);
	}

	public static WritableDeployment getDefaultDeployment(UUID deploybaleEntityId) {
		return new WritableDeployment(deploybaleEntityId, DeployableStatus.NotScheduled, null, 0, 0);
	}

	@Override
	public UUID getId() {
		return id.get();
	}

	public UUID getProjectVersionId() {
		return projectVersionId.get();
	}

	public DeployableStatus getStatus() {
		return status.get();
	}

	public void setStatus(DeployableStatus status) {
		this.status.set(status);
	}

	public Optional<Instant> getTimestamp() {
		return Optional.ofNullable(timestamp.get());
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp.set(timestamp);
	}

	public ReadableDeployment readable() {
		return ReadableDeployment.builder()
				.id(getId())
				.projectVersionId(getProjectVersionId())
				.status(getStatus())
				.timestamp(timestamp.get())
				.build();
	}
}
