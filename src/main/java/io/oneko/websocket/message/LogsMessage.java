package io.oneko.websocket.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogsMessage implements ONekoWebSocketMessage {
	Date timestamp;
	List<String> lines;
}
