package io.oneko.websocket;

import java.util.Arrays;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.databind.JsonNode;

import io.oneko.websocket.message.ONekoWebSocketMessage;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.EmitterProcessor;

@Data
@Builder
@Slf4j
public class WebSocketSessionContext {
	private final String id;
	private final WebSocketSession session;
	private final EmitterProcessor<JsonNode> inStream;
	private final EmitterProcessor<ONekoWebSocketMessage> outStream;

	public static WebSocketSessionContext of(WebSocketSession wsSession) {
		if (!wsSession.getHandshakeInfo().getHeaders().containsKey(HttpHeaders.COOKIE)) {
			throw new RuntimeException("No Session Cookie provided");
		}

		String[] cookies = wsSession.getHandshakeInfo().getHeaders().get("cookie").get(0).split(";");

		// The session id "SESSION" is send via a cookie. We have to extract that id in order to connect
		// it to our HTTP Session.
		String sessionId = Arrays.stream(cookies)
				.map(String::trim)
				.filter(cookie -> cookie.startsWith("SESSION"))
				.map(cookie -> cookie.split("=")[1].trim())
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Session does not contain any session id"));

		return WebSocketSessionContext.builder()
				.session(wsSession)
				.id(sessionId)
				.inStream(EmitterProcessor.create())
				.outStream(EmitterProcessor.create())
				.build();
	}

	public void close() {
		log.debug("Closing WebSocket session", this.id);
		inStream.onComplete();
		outStream.onComplete();
		session.close().doOnError(throwable -> log.debug("An error occurred while closing a websocket session. Maybe the session was already closed.")).subscribe();
	}
}
