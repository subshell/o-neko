package io.oneko.event;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Service
@Slf4j
public class EventDispatcher {

	private EmitterProcessor<Event> emitterProcessor = EmitterProcessor.create();

	public Flux<Event> streamEvents() {
		return emitterProcessor;
	}

	/**
	 * Creates and dispatches an event.
	 * This method implicitly calls the given creator with the current event trigger.
	 * <br/>
	 * Use this method if you are creating events without a specific Mono to retrieve your data from.
	 */
	public void createAndDispatchEvent(Function<EventTrigger, Event> eventCreator) {
		Mono.subscriberContext()
				.map(context -> this.mapTupleToEntity(Tuples.of(context, context), (t1, t2) -> eventCreator.apply(t2)))
				.subscribe();
	}

	/**
	 * Uses the given mono to obtain an instance of T which is then fed into the given event creator together with the
	 * current event trigger in order to generate a business event. The Event will be dispatched straight away.
	 *
	 * @param mono         The source for the object to convert into events.
	 * @param eventCreator The factory for actually creating events.
	 * @param <T>          The source object data type
	 * @return A mono with a mapper for creating events being added.
	 */
	public <T> Mono<T> createAndDispatchEvent(Mono<T> mono, BiFunction<T, EventTrigger, Event> eventCreator) {
		return mono.zipWith(Mono.subscriberContext())
				.map(t -> this.mapTupleToEntity(t, eventCreator));
	}

	/**
	 * Uses the given flux to obtain an instance of T which is then fed into the given event creator together with the
	 * current event trigger in order to generate a business event. The Event will be dispatched straight away.
	 *
	 * @param flux         The source for the object to convert into events.
	 * @param eventCreator The factory for actually creating events.
	 * @param <T>          The source object data type
	 * @return A flux with a mapper for creating events being added.
	 */
	public <T> Flux<T> createAndDispatchEvent(Flux<T> flux, BiFunction<T, EventTrigger, Event> eventCreator) {
		return flux.zipWith(Mono.subscriberContext())
				.map(t -> this.mapTupleToEntity(t, eventCreator));
	}

	private <T> T mapTupleToEntity(Tuple2<T, Context> tuple, BiFunction<T, EventTrigger, Event> eventCreator) {
		T element = tuple.getT1();
		Context context = tuple.getT2();
		EventTrigger eventTrigger = context.getOrDefault(EventTrigger.class, UnknownTrigger.INSTANCE);
		Event event = eventCreator.apply(element, eventTrigger);
		this.dispatch(event);
		return element;
	}

	public void dispatch(Event event) {
		this.emitterProcessor.onNext(event);
	}

}
