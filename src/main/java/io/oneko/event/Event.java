package io.oneko.event;

import java.time.LocalDateTime;
import java.util.UUID;

import io.oneko.domain.Identifiable;
import lombok.Getter;

/**
 * Represents a business event.
 * All implementations should be immutable.
 */
@Getter
public abstract class Event extends Identifiable {

	private final UUID id;
	private final LocalDateTime creationDate;
	private final EventTrigger trigger;

	protected Event(EventTrigger trigger) {
		this.trigger = trigger;
		this.id = UUID.randomUUID();
		this.creationDate = LocalDateTime.now();
	}

	/**
	 * Creates a human readable string representation of what happened.
	 * This should specifically cover the business properties fo the event's subclass.
	 */
	public abstract String humanReadable();
}
