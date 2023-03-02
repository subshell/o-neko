package io.oneko.activity.internal;

import io.oneko.activity.Activity;
import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.event.Event;
import io.oneko.event.EventDispatcher;
import io.oneko.websocket.SessionWebSocketHandler;
import io.oneko.websocket.message.ActivityMessage;
import jakarta.annotation.PreDestroy;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Listens to events adn converts them to activities dispatching them into the persistence and via websocket to the frontends.
 */
@Component
@Slf4j
public class EventsToActivities {

	private final WritableActivityLog activityLog;
	private final SessionWebSocketHandler webSocketHandler;
	private final EventDispatcher eventDispatcher;
	private final Consumer<Event> eventListener = this::processEvent;

	@Autowired
	public EventsToActivities(WritableActivityLog activityLog,
			SessionWebSocketHandler webSocketHandler,
			EventDispatcher eventDispatcher) {
		this.activityLog = activityLog;
		this.webSocketHandler = webSocketHandler;
		this.eventDispatcher = eventDispatcher;
		eventDispatcher.registerListener(eventListener);
	}

	@PreDestroy
	public void cleanup() {
		eventDispatcher.removeListener(eventListener);
	}

	private void processEvent(Event event) {
		Activity.ActivityBuilder activityBuilder = Activity.builder()
				.id(event.getId())
				.date(event.getCreationDate())
				.title(event.title())
				.description(event.description())
				.priority(event.getTrigger()
						.priority())
				.triggerType(event.getTrigger()
						.getType())
				.triggerName(event.getTrigger()
						.humanReadable());

		if (event instanceof EntityChangedEvent) {
			EntityChangedEvent ece = (EntityChangedEvent) event;
			DescribingEntityChange description = ece.describeEntityChange();
			DescribingEntityChange entityChange = DescribingEntityChange.builder()
					.id(description.getId())
					.name(description.getName())
					.entityType(description.getEntityType())
					.changeType(description.getChangeType())
					.changedProperties(description.getChangedProperties())
					.build();
			activityBuilder.changedEntity(entityChange);
		} else {
			activityBuilder.activityType(event.getClass()
					.getSimpleName());
		}

		Activity activity = activityBuilder.build();
		Activity persistedActivity = activityLog.addActivity(activity);
		webSocketHandler.broadcast(new ActivityMessage(persistedActivity));
	}
}
