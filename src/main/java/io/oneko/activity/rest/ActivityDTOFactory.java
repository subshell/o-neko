package io.oneko.activity.rest;

import org.springframework.stereotype.Component;

import io.oneko.activity.Activity;

@Component
public class ActivityDTOFactory {

	public ActivityDTO create(Activity a) {
		ActivityDTO.ActivityDTOBuilder builder = ActivityDTO.builder()
				.id(a.getId())
				.date(a.getDate())
				.description(a.getDescription())
				.triggerName(a.getTriggerName())
				.triggerType(a.getTriggerType())
				.priority(a.getPriority());
		a.getChangedEntity().ifPresent(description -> builder.entityId(description.getId())
				.entityName(description.getName())
				.entityType(description.getEntityType())
				.changeType(description.getChangeType())
				.changedEntityProperties(description.getChangedProperties()));
		return builder.build();
	}
}
