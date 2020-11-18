package io.oneko.project.persistence;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.oneko.event.*;
import io.oneko.project.event.EventAwareProjectRepository;
import io.oneko.project.event.ProjectSavedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.oneko.project.WritableProject;

class EventAwareProjectRepositoryTest {

	private final CurrentEventTrigger currentEventTrigger = new CurrentEventTrigger();
	private EventAwareProjectRepository uut;
	private List<Event> currentEvents;

	@BeforeEach
	void setup() {
		this.currentEvents = new ArrayList<>();
		EventDispatcher dispatcher = new EventDispatcher(currentEventTrigger);
		dispatcher.streamEvents().subscribe(this.currentEvents::add);
		this.uut = new ProjectInMemoryRepository(dispatcher);
	}

	@AfterEach
	void clear() {
		currentEventTrigger.unset();
	}

	@Test
	void testSaveEvent() {
		WritableProject p = new WritableProject(UUID.randomUUID());

		this.uut.add(p);

		assertThat(this.currentEvents, hasItem(isA(ProjectSavedEvent.class)));
		ProjectSavedEvent event = (ProjectSavedEvent) this.currentEvents.get(0);
		assertThat(event.describeEntityChange().getId(), is(p.getId()));
		assertThat(event.getTrigger(), is(instanceOf(UnknownTrigger.class)));
	}

	@Test
	void testSaveEventWithTrigger() {
		EventTrigger customTrigger = new SampleTrigger();
		currentEventTrigger.setCurrentTrigger(customTrigger);
		WritableProject p = new WritableProject(UUID.randomUUID());

		this.uut.add(p);

		assertThat(this.currentEvents, hasItem(isA(ProjectSavedEvent.class)));
		ProjectSavedEvent event = (ProjectSavedEvent) this.currentEvents.get(0);
		assertThat(event.getTrigger(), is(instanceOf(SampleTrigger.class)));
	}

}
