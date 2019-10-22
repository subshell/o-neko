package io.oneko.project.event;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.oneko.docker.DockerRegistry;
import io.oneko.event.Event;
import io.oneko.event.EventDispatcher;
import io.oneko.event.EventTrigger;
import io.oneko.event.SampleTrigger;
import io.oneko.event.UnknownTrigger;
import io.oneko.project.Project;
import reactor.util.context.Context;

public class EventAwareProjectRepositoryTest {

	private EventAwareProjectRepository uut;
	private List<Event> currentEvents;

	@Before
	public void setup() {
		this.currentEvents = new ArrayList<>();
		EventDispatcher dispatcher = new EventDispatcher();
		dispatcher.streamEvents().subscribe(this.currentEvents::add);
		this.uut = new InMemoryProjectRepository(dispatcher);
	}

	@Test
	public void testSaveEvent() {
		DockerRegistry dr = new DockerRegistry();
		Project p = new Project(dr);

		this.uut.add(p).subscribe();

		assertThat(this.currentEvents, hasItem(isA(ProjectSavedEvent.class)));
		ProjectSavedEvent event = (ProjectSavedEvent) this.currentEvents.get(0);
		assertThat(event.describeEntityChange().getId(), is(p.getId()));
		assertThat(event.getTrigger(), is(instanceOf(UnknownTrigger.class)));
	}

	@Test
	public void testSaveEventWithTrigger() {
		EventTrigger customTrigger = new SampleTrigger();
		DockerRegistry dr = new DockerRegistry();
		Project p = new Project(dr);

		this.uut.add(p).subscriberContext(Context.of(EventTrigger.class, customTrigger)).subscribe();

		assertThat(this.currentEvents, hasItem(isA(ProjectSavedEvent.class)));
		ProjectSavedEvent event = (ProjectSavedEvent) this.currentEvents.get(0);
		assertThat(event.getTrigger(), is(instanceOf(SampleTrigger.class)));
	}

}
