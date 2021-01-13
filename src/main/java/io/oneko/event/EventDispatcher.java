package io.oneko.event;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;


@Service
public class EventDispatcher {

	private final Set<Consumer<Event>> listeners = new HashSet<>();
	private final CurrentEventTrigger currentEventTrigger;

	public EventDispatcher(CurrentEventTrigger currentEventTrigger) {
		this.currentEventTrigger = currentEventTrigger;
	}

	public void registerListener(Consumer<Event> listener) {
		listeners.add(listener);
	}

	public void removeListener(Consumer<Event> listener) {
		listeners.remove(listener);
	}

	public void dispatch(Event event) {
		if (event.getTrigger() == null) {
			event.setTrigger(currentEventTrigger.currentTrigger().orElse(UnknownTrigger.INSTANCE));
		}

		listeners.forEach(listener -> listener.accept(event));
	}

}
