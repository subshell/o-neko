package io.oneko.websocket.message;

import io.oneko.activity.Activity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityMessage implements ONekoWebSocketMessage {
	private Activity activity;
}
