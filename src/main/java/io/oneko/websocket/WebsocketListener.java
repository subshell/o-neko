package io.oneko.websocket;

import io.oneko.websocket.message.ONekoWebSocketMessage;

public interface WebsocketListener {

    void onMessage(ONekoWebSocketMessage message, String sessionId);

    default void sessionClosed(String sessionId) {
    }

}
