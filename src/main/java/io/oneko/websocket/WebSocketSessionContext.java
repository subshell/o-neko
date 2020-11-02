package io.oneko.websocket;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.http.HttpHeaders;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.web.socket.WebSocketSession;

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
		final var httpHeaders = wsSession.getHandshakeHeaders();
		if (!httpHeaders.containsKey(HttpHeaders.COOKIE)) {
			throw new SessionAuthenticationException("No Session Cookie provided");
		}

		final String cookieString = httpHeaders.get(HttpHeaders.COOKIE).get(0);
		String[] cookies = cookieString.split(";");

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
		log.debug("Closing WebSocket session {}", id);
		inStream.onComplete();
		outStream.onComplete();

		try {
			session.close();
		} catch (IOException e) {
			log.error("An error occurred while closing a websocket session. Maybe the session was already closed.", e);
		}
	}
}
