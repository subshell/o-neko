package io.oneko.event;

import java.time.LocalDateTime;
import java.util.UUID;

import com.google.common.base.Preconditions;

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
	private EventTrigger trigger;

	protected Event() {
		this.id = UUID.randomUUID();
		this.creationDate = LocalDateTime.now();
	}

	public void setTrigger(EventTrigger trigger) {
		Preconditions.checkState(this.trigger == null, "An events trigger can only be set once");
		this.trigger = trigger;
	}

	/**
	 * Creates a human-readable name of what happened.
	 * This should specifically cover the business properties fo the event's subclass.
	 */
	public abstract String name();

	/**
	 * Creates a human-readable description of what happened.
	 * This should specifically cover the business properties fo the event's subclass.
	 */
	public String description() {
		return "";
	}
}
