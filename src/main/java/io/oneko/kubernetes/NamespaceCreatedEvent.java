package io.oneko.kubernetes;

import io.oneko.event.Event;
import io.oneko.event.EventTrigger;
import lombok.Getter;

public class NamespaceCreatedEvent extends Event {

	@Getter
	private final String namespace;

	public NamespaceCreatedEvent(String namespace, EventTrigger trigger) {
		super(trigger);
		this.namespace = namespace;
	}

	@Override
	public String humanReadable() {
		return "Namespace " + namespace + " has been created in kubernetes.";
	}
}
