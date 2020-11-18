package io.oneko.kubernetes.deployments;

import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareProperty;
import lombok.Builder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class Deployment extends ModificationAwareIdentifiable {

	private ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
	private ModificationAwareProperty<UUID> deployableId = new ModificationAwareProperty<>(this, "deployableId");
	private ModificationAwareProperty<DeployableStatus> status = new ModificationAwareProperty<>(this, "status");
	private ModificationAwareProperty<Instant> timestamp = new ModificationAwareProperty<>(this, "timestamp");
	private ModificationAwareProperty<Integer> containerCount = new ModificationAwareProperty<>(this, "containerCount");
	private ModificationAwareProperty<Integer> readyContainerCount = new ModificationAwareProperty<>(this, "readyContainerCount");

	public Deployment(UUID projectVersionID, DeployableStatus status, Instant timestamp, int containerCount, int readyContainerCount) {
		this.id.set(UUID.randomUUID());
		this.deployableId.set(projectVersionID);
		this.status.set(status);
		this.timestamp.set(timestamp);
		this.containerCount.set(containerCount);
		this.readyContainerCount.set(readyContainerCount);
	}

	@Builder
	public Deployment(UUID id, UUID deployableId, DeployableStatus status, Instant timestamp, int containerCount, int readyContainerCount) {
		this.id.init(id);
		this.deployableId.init(deployableId);
		this.status.init(status);
		this.timestamp.init(timestamp);
		this.containerCount.init(containerCount);
		this.readyContainerCount.init(readyContainerCount);
	}

	public static Deployment getDefaultDeployment(DeploymentBehaviour behaviour, UUID deploybaleEntityId) {
		return new Deployment(deploybaleEntityId, DeployableStatus.NotScheduled, null, 0, 0);
	}

	@Override
	public UUID getId() {
		return id.get();
	}

	public UUID getDeployableId() {
		return deployableId.get();
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

	public int getContainerCount() {
		return containerCount.get();
	}

	public void setContainerCount(int containerCount) {
		this.containerCount.set(containerCount);
	}

	public int getReadyContainerCount() {
		return readyContainerCount.get();
	}

	public void setReadyContainerCount(int readyContainerCount) {
		this.readyContainerCount.set(readyContainerCount);
	}
}
