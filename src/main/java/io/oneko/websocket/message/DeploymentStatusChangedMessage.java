package io.oneko.websocket.message;

import java.time.Instant;
import java.util.UUID;

import io.oneko.kubernetes.deployments.DeployableStatus;
import io.oneko.kubernetes.deployments.DesiredState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeploymentStatusChangedMessage implements ONekoWebSocketMessage {

	private UUID deployableId;
	private UUID ownerId;
	private DeployableType deployableType;
	private DeployableStatus status;
	private DesiredState desiredState;
	private Instant timestamp;
	private boolean outdated;
	private Instant imageUpdatedDate;
	public enum DeployableType {
		projectVersion, meshComponent
	}
}
