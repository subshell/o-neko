package io.oneko.event;

import org.springframework.stereotype.Service;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

@Service
public class EventDispatcher {

	private final EmitterProcessor<Event> emitterProcessor = EmitterProcessor.create();
	private final CurrentEventTrigger currentEventTrigger;

	public EventDispatcher(CurrentEventTrigger currentEventTrigger) {
		this.currentEventTrigger = currentEventTrigger;
	}

	public Flux<Event> streamEvents() {
		return emitterProcessor;
	}

	public void dispatch(Event event) {
		if (event.getTrigger() == null) {
			event.setTrigger(currentEventTrigger.currentTrigger().orElse(UnknownTrigger.INSTANCE));
		}
		this.emitterProcessor.onNext(event);
	}

}
