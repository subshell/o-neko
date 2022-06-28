package io.oneko.kubernetes;

import io.oneko.event.Event;
import lombok.Getter;

public class NamespaceCreatedEvent extends Event {

	@Getter
	private final String namespace;

	public NamespaceCreatedEvent(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public String title() {
		return String.format("Namespace %s as been created in kubernetes.", namespace);
	}
}
