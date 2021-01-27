package io.oneko.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.oneko.websocket.message.ONekoWebSocketMessage;
import lombok.extern.slf4j.Slf4j;

// https://books.google.de/books?id=EkBPDwAAQBAJ&pg=PA320&lpg=PA320&dq=getHandshakeInfo().getPrincipal()&source=bl&ots=9nchCL8YFm&sig=m-xV7tPCNjRh8bzi23xdx_xBWPY&hl=de&sa=X&ved=0ahUKEwiCsoLhg6ncAhWCjKQKHbWQAWwQ6AEIJzAA#v=onepage&q=getHandshakeInfo().getPrincipal()&f=false

@Slf4j
@Service
public class SessionWebSocketHandler extends TextWebSocketHandler {
	private final Map<String, WebSocketSessionContext> sessionContextMap = new HashMap<>();
	private final ObjectMapper objectMapper;


	public SessionWebSocketHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		WebSocketSessionContext sessionContext = WebSocketSessionContext.of(session);
		sessionContextMap.put(sessionContext.getId(), sessionContext);
		log.trace("New client ws connection {} established", sessionContext.getId());
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) {
		log.trace("Error while transporting websocket message for {}", session.getId(), exception);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
		invalidateSession(session.getId());
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		final String payload = message.getPayload();
		final ONekoWebSocketMessage msgObj = this.payloadToMessage(payload);
		if (msgObj == null) {
			return;
		}
		// Currently, we do not handle incoming webSocket messages, so we just log them
		log.trace("Received WebSocket message:\n{}", msgObj.toString());
	}

	public void invalidateSession(String sessionId) {
		if (!sessionContextMap.containsKey(sessionId)) {
			return;
		}

		sessionContextMap.get(sessionId).close();
		sessionContextMap.remove(sessionId);
	}

	public void send(WebSocketSession session, ONekoWebSocketMessage message) {
		try {
			var textMessage = new TextMessage(Objects.requireNonNull(this.messageToPayload(message)));
			session.sendMessage(textMessage);
		} catch (IOException e) {
			log.error("Error while sending the message {}", message);
		}
	}


	public void send(String sessionId, ONekoWebSocketMessage message) {
		if (!sessionContextMap.containsKey(sessionId)) {
			log.trace("User with session id {} does not exist.", sessionId);
			return;
		}

		WebSocketSession session = sessionContextMap.get(sessionId).getSession();
		send(session, message);
	}

	public void broadcast(ONekoWebSocketMessage message) {
		for (WebSocketSessionContext ctx : new ArrayList<>(sessionContextMap.values())) {
			WebSocketSession session = ctx.getSession();
			if (!session.isOpen()) {
				log.debug("Ws session with id {} is already closed, we skip this one.", ctx.getId());
				invalidateSession(ctx.getId());
				continue;
			}

			send(session, message);
		}
	}

	private String messageToPayload(ONekoWebSocketMessage message) {
		try {
			return this.objectMapper.writeValueAsString(message);
		} catch (JsonProcessingException e) {
			log.error("", e);
		}

		return null;
	}

	private ONekoWebSocketMessage payloadToMessage(String payload) {
		try {
			return objectMapper.readValue(payload, ONekoWebSocketMessage.class);
		} catch (IOException e) {
			log.error("Error parsing the websocket message payload", e);
		}

		return null;
	}
}
