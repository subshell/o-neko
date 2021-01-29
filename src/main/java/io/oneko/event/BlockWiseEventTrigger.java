package io.oneko.event;

import lombok.Getter;

/**
 * Convenience class for setting and unsetting an event trigger at {@link CurrentEventTrigger}
 */
public class BlockWiseEventTrigger implements AutoCloseable {

	@Getter
	private final EventTrigger trigger;
	private final CurrentEventTrigger currentEventTrigger;

	BlockWiseEventTrigger(EventTrigger trigger, CurrentEventTrigger currentEventTrigger) {
		this.trigger = trigger;
		this.currentEventTrigger = currentEventTrigger;
		currentEventTrigger.setCurrentTrigger(trigger);
	}

	@Override
	public void close() {
		currentEventTrigger.unset();
	}
}
