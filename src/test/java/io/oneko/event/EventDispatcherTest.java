package io.oneko.event;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class EventDispatcherTest {

	private List<Event> currentEvents = new ArrayList<>();
	private EventDispatcher uut = new EventDispatcher();

	@Before
	public void setup() {
		uut.streamEvents().subscribe(this.currentEvents::add);
	}

	@Test
	public void testDefaultTriggerIntoMono() {
		String myTestString = "Weasel rules";
		Mono<String> mono = Mono.just(myTestString);

		assertThat(currentEvents, is(empty()));

		uut.createAndDispatchEvent(mono, SampleEvent::new).subscribe();

		assertThat(currentEvents, hasSize(1));
		assertThat(currentEvents.get(0).getTrigger(), is(instanceOf(UnknownTrigger.class)));
	}

	@Test
	public void testReadSetTrigger() {
		String myTestString = "Weasel rules";
		Mono<String> mono = Mono.just(myTestString);

		assertThat(currentEvents, is(empty()));

		uut.createAndDispatchEvent(mono, SampleEvent::new)
				.subscriberContext(Context.of(EventTrigger.class, new SampleTrigger()))
				.subscribe();

		assertThat(currentEvents, hasSize(1));
		assertThat(currentEvents.get(0).getTrigger(), is(instanceOf(SampleTrigger.class)));
	}

	@Test
	public void showCaseForWrongUsage() {
		Mono<String> mono = Mono.just("Weasel rules");

		assertThat(currentEvents, is(empty()));


		Mono<String> stringMono = mono.subscriberContext(Context.of(EventTrigger.class, new SampleTrigger()));
		uut.createAndDispatchEvent(stringMono, SampleEvent::new).subscribe();

		assertThat(currentEvents, hasSize(1));
		/*
		 * This here is actually weird:
		 * The subscriberContext with the trigger has to be added after the actual call to createAndDispatchEvent.
		 * Due to that the assert is NOT(instanceOf(SampleTrigger.class))
		 */
		assertThat(currentEvents.get(0).getTrigger(), is(not(instanceOf(SampleTrigger.class))));
	}

	@Test
	public void showCaseThenUsageVersion1() {
		Mono<String> mono = Mono.just("Weasel rules");

		uut.createAndDispatchEvent(mono, SampleEvent::new)
				.then(Mono.just(42L))
				//here: first add the then() before adding the subscriber context
				.subscriberContext(Context.of(EventTrigger.class, new SampleTrigger()))
				.subscribe();

		assertThat(currentEvents.get(0).getTrigger(), is(instanceOf(SampleTrigger.class)));
	}

	@Test
	public void showCaseThenUsageVersion2() {
		Mono<String> mono = Mono.just("Weasel rules");

		uut.createAndDispatchEvent(mono, SampleEvent::new)
				//this time: first add the context and then call for then()
				.subscriberContext(Context.of(EventTrigger.class, new SampleTrigger()))
				.then(Mono.just(42L))
				.subscribe();

		assertThat(currentEvents.get(0).getTrigger(), is(instanceOf(SampleTrigger.class)));
	}

	@Test
	public void showCaseVoidMono() {
		Mono<Void> mono = Mono.just("Weasel rules").then();

		assertThat(currentEvents, is(empty()));

		uut.createAndDispatchEvent(mono, SampleEvent::new)
				.subscriberContext(Context.of(EventTrigger.class, new SampleTrigger()))
				.subscribe();
		// the mono never emits a value.
		assertThat(currentEvents, hasSize(0));
	}

}
