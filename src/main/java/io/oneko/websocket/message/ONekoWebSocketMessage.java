package io.oneko.websocket.message;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
		@Type(TestMessage.class),
		@Type(DeploymentStatusChangedMessage.class),
		@Type(ActivityMessage.class)
})
public interface ONekoWebSocketMessage {

}
