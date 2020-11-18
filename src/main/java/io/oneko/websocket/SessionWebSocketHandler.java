package io.oneko.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.oneko.websocket.message.ONekoWebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// https://books.google.de/books?id=EkBPDwAAQBAJ&pg=PA320&lpg=PA320&dq=getHandshakeInfo().getPrincipal()&source=bl&ots=9nchCL8YFm&sig=m-xV7tPCNjRh8bzi23xdx_xBWPY&hl=de&sa=X&ved=0ahUKEwiCsoLhg6ncAhWCjKQKHbWQAWwQ6AEIJzAA#v=onepage&q=getHandshakeInfo().getPrincipal()&f=false

@Slf4j
@Service
public class SessionWebSocketHandler extends TextWebSocketHandler {
	private final EmitterProcessor<ONekoWebSocketMessage> inStream = EmitterProcessor.create();
	private final Map<String, WebSocketSessionContext> sessionContextMap = new HashMap<>();
	private final ObjectMapper objectMapper;


	public SessionWebSocketHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;

		// Currently, we do not handle incoming webSocket messages
		this.inStream.subscribe(msg -> log.trace("Received WebSocket message:\n{}", msg.toString()));
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		WebSocketSessionContext sessionContext = WebSocketSessionContext.of(session);
		sessionContext.getOutStream()
				.map(payload -> new TextMessage(Objects.requireNonNull(this.messageToPayload(payload))))
				.subscribe(message -> {
					try {
						session.sendMessage(message);
					} catch (IOException e) {
						log.error("Error while sending the message {}", message);
					}
				});

		sessionContextMap.put(sessionContext.getId(), sessionContext);
		log.debug("New client ws connection {} established", sessionContext.getId());
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) {
		log.error("Error while transporting webSocket message for {}", session.getId(), exception);
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
		inStream.onNext(msgObj);
	}

	public void invalidateSession(String sessionId) {
		if (!sessionContextMap.containsKey(sessionId)) {
			return;
		}

		sessionContextMap.get(sessionId).close();
		sessionContextMap.remove(sessionId);
	}

	public void send(String sessionId, ONekoWebSocketMessage message) {
		if (!sessionContextMap.containsKey(sessionId)) {
			log.debug("User with session id {} does not exist.", sessionId);
			return;
		}

		sessionContextMap.get(sessionId).getOutStream().onNext(message);
	}

	public void broadcast(ONekoWebSocketMessage message) {
		for (WebSocketSessionContext ctx : new ArrayList<>(sessionContextMap.values())) {
			final EmitterProcessor<ONekoWebSocketMessage> outStream = ctx.getOutStream();
			if (outStream.isCancelled() || outStream.isTerminated() || outStream.isDisposed()) {
				log.debug("Output stream of ws session with id {} is already closed, we skip this one.", ctx.getId());
				invalidateSession(ctx.getId());
				continue;
			}
			outStream.onNext(message);
		}
	}

	public Flux<ONekoWebSocketMessage> stream() {
		return this.inStream;
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
