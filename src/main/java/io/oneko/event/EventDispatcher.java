package io.oneko.event;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.oneko.metrics.MetricNameBuilder;


@Service
public class EventDispatcher {

	private final Set<Consumer<Event>> listeners = new HashSet<>();
	private final CurrentEventTrigger currentEventTrigger;

	private final Counter eventsCounter;

	public EventDispatcher(CurrentEventTrigger currentEventTrigger, MeterRegistry meterRegistry) {
		this.currentEventTrigger = currentEventTrigger;
		eventsCounter = Counter.builder(new MetricNameBuilder().amountOf("events.dispatched").build())
				.description("the number of events dispatched by the EventDispatcher since application start")
				.register(meterRegistry);
	}

	public void registerListener(Consumer<Event> listener) {
		listeners.add(listener);
	}

	public void removeListener(Consumer<Event> listener) {
		listeners.remove(listener);
	}

	public void dispatch(Event event) {
		eventsCounter.increment();
		if (event.getTrigger() == null) {
			event.setTrigger(currentEventTrigger.currentTrigger().orElse(UnknownTrigger.INSTANCE));
		}
		listeners.forEach(listener -> listener.accept(event));
	}

}
