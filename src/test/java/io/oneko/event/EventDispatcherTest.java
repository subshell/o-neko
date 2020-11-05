package io.oneko.event;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventDispatcherTest {

	private final List<Event> currentEvents = new ArrayList<>();
	private final CurrentEventTrigger currentEventTrigger = new CurrentEventTrigger();
	private final EventDispatcher uut = new EventDispatcher(currentEventTrigger);


	@BeforeEach
	void setup() {
		uut.streamEvents().subscribe(this.currentEvents::add);
	}

	@AfterEach
	void clear() {
		currentEventTrigger.unset();
	}

	@Test
	void testDefaultTriggerIntoMono() {
		String myTestString = "Weasel rules";
		assertThat(currentEvents).isEmpty();

		uut.dispatch(new SampleEvent(myTestString));

		assertThat(currentEvents).hasSize(1);
		assertThat(currentEvents.get(0).getTrigger()).isInstanceOf(UnknownTrigger.class);
	}

	@Test
	void testReadSetTrigger() {
		String myTestString = "Weasel rules";
		assertThat(currentEvents).isEmpty();

		currentEventTrigger.setCurrentTrigger(new SampleTrigger());
		uut.dispatch(new SampleEvent(myTestString));

		assertThat(currentEvents).hasSize(1);
		assertThat(currentEvents.get(0).getTrigger()).isInstanceOf(SampleTrigger.class);
	}


}
