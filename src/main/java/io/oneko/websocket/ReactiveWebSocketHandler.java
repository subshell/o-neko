package io.oneko.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.oneko.websocket.message.ONekoWebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


// https://books.google.de/books?id=EkBPDwAAQBAJ&pg=PA320&lpg=PA320&dq=getHandshakeInfo().getPrincipal()&source=bl&ots=9nchCL8YFm&sig=m-xV7tPCNjRh8bzi23xdx_xBWPY&hl=de&sa=X&ved=0ahUKEwiCsoLhg6ncAhWCjKQKHbWQAWwQ6AEIJzAA#v=onepage&q=getHandshakeInfo().getPrincipal()&f=false

@Slf4j
@Service
public class ReactiveWebSocketHandler implements WebSocketHandler {
	private final EmitterProcessor<ONekoWebSocketMessage> inStream = EmitterProcessor.create();

	private final Map<String, WebSocketSessionContext> sessionContextMap = new HashMap<>();

	private final ObjectMapper objectMapper;


	public ReactiveWebSocketHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.inStream.subscribe(msg -> System.out.println(msg));
	}

	@Override
	public Mono<Void> handle(WebSocketSession webSocketSession) {
		WebSocketSessionContext sessionContext = WebSocketSessionContext.of(webSocketSession);

		sessionContextMap.put(sessionContext.getId(), sessionContext);

		log.debug("New client ws connection established", sessionContext.getId());

		return webSocketSession
				.send(sessionContext.getOutStream().map(this::messageToPayload).map(webSocketSession::textMessage))
				.and(webSocketSession.receive().map(WebSocketMessage::getPayloadAsText).map(this::payloadToMessage).map(payload -> {
					log.debug("Received WebSocket message", payload);
					inStream.onNext(payload);
					return payload;
				}));
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
			log.error("", e);
		}

		return null;
	}
}