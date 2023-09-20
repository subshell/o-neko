package io.oneko.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscribeToLogsMessage implements ONekoWebSocketMessage {
	UUID projectId;
	UUID versionId;
	String pod;
	String container;
}
