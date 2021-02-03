package io.oneko.websocket.message;

import io.oneko.kubernetes.deployments.DeployableStatus;
import io.oneko.kubernetes.deployments.DesiredState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeploymentStatusChangedMessage implements ONekoWebSocketMessage {

	private UUID projectVersionId;
	private UUID ownerId;
	private DeployableStatus status;
	private DesiredState desiredState;
	private Instant timestamp;
	private boolean outdated;
	private Instant imageUpdatedDate;

}
