package io.oneko.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.oneko.metrics.MetricNameBuilder;
import io.oneko.websocket.message.ONekoWebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.kv;

// https://books.google.de/books?id=EkBPDwAAQBAJ&pg=PA320&lpg=PA320&dq=getHandshakeInfo().getPrincipal()&source=bl&ots=9nchCL8YFm&sig=m-xV7tPCNjRh8bzi23xdx_xBWPY&hl=de&sa=X&ved=0ahUKEwiCsoLhg6ncAhWCjKQKHbWQAWwQ6AEIJzAA#v=onepage&q=getHandshakeInfo().getPrincipal()&f=false

@Slf4j
@Service
public class SessionWebSocketHandler extends TextWebSocketHandler {
	private final Map<String, WebSocketSessionContext> sessionContextMap = new HashMap<>();
	private final ObjectMapper objectMapper;

	private final Counter receivedWebsocketMessageCounter;
	private final Counter sentWebsocketMessageCounter;
	private Set<WebsocketListener> listeners = Sets.newConcurrentHashSet();

	public SessionWebSocketHandler(ObjectMapper objectMapper, MeterRegistry meterRegistry) {
		this.objectMapper = objectMapper;
		Gauge.builder(new MetricNameBuilder().amountOf("websocket.sessions").build(), sessionContextMap::size)
			.register(meterRegistry);
		this.receivedWebsocketMessageCounter = Counter.builder(new MetricNameBuilder().amountOf("websocket.messages").build())
			.tag("type", "received")
			.register(meterRegistry);
		this.sentWebsocketMessageCounter = Counter.builder(new MetricNameBuilder().amountOf("websocket.messages").build())
			.tag("type", "sent")
			.register(meterRegistry);
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		WebSocketSessionContext sessionContext = WebSocketSessionContext.of(session);
		sessionContextMap.put(sessionContext.getWsSessionId(), sessionContext);
		log.trace("new client websocket connection established ({}, {})", kv("session_id", sessionContext.getWsSessionId()), kv("total_websocket_session_count", sessionContextMap.size()));
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) {
		log.trace("error while transporting websocket message ({})", kv("session_id", session.getId()), exception);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
		invalidateWsSession(session.getId());
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
		log.trace("received websocket message ({})", kv("message", msgObj.toString()));
		listeners.forEach(l -> l.onMessage(msgObj, session.getId()));
		receivedWebsocketMessageCounter.increment();
	}

	public void invalidateWsSession(String wsSessionId) {
		if (!sessionContextMap.containsKey(wsSessionId)) {
			return;
		}

		sessionContextMap.get(wsSessionId).close();
		sessionContextMap.remove(wsSessionId);
		listeners.forEach(l -> l.sessionClosed(wsSessionId));
		log.trace("removing websocket connection ({}, {})", kv("session_id", wsSessionId), kv("total_websocket_session_count", sessionContextMap.size()));
	}

	public void invalidateUserSession(String userSessionId) {
		sessionContextMap.values().stream()
			.filter(sessionsContext -> StringUtils.equals(sessionsContext.getUserSessionId(), userSessionId))
			.forEach((wsSessionContext) -> invalidateUserSession(wsSessionContext.getWsSessionId()));
	}

	public void send(WebSocketSession session, ONekoWebSocketMessage message) {
		try {
			var textMessage = new TextMessage(Objects.requireNonNull(this.messageToPayload(message)));
			session.sendMessage(textMessage);
			sentWebsocketMessageCounter.increment();
		} catch (IOException e) {
			log.error("error while sending websocket message ({})", kv("message", message));
		}
	}

	public void send(String sessionId, ONekoWebSocketMessage message) {
		if (!sessionContextMap.containsKey(sessionId)) {
			log.trace("no matching user found for session id ({})", kv("session_id", sessionId));
			return;
		}

		WebSocketSession session = sessionContextMap.get(sessionId).getSession();
		send(session, message);
	}

	public void broadcast(ONekoWebSocketMessage message) {
		for (WebSocketSessionContext ctx : sessionContextMap.values()) {
			WebSocketSession session = ctx.getSession();
			if (!session.isOpen()) {
				log.trace("websocket session is already closed ({})", kv("session_id", ctx.getWsSessionId()));
				invalidateWsSession(ctx.getWsSessionId());
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
			log.error("error parsing websocket message payload", e);
		}

		return null;
	}

	public void registerListener(WebsocketListener listener) {
		this.listeners.add(listener);
	}
}
