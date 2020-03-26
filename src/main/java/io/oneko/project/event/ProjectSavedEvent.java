package io.oneko.project.event;

import java.util.Collection;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.event.EventTrigger;
import io.oneko.project.WritableProject;

public class ProjectSavedEvent extends EntityChangedEvent {

	/**
	 * Use this constructor only with the project prior to actually saving it.
	 */
	public ProjectSavedEvent(WritableProject project, EventTrigger trigger) {
		this(project, project.getDirtyProperties(), trigger);
	}

	public ProjectSavedEvent(WritableProject project, Collection<String> changedProperties, EventTrigger trigger) {
		super(trigger, DescribingEntityChange.builder()
				.id(project.getUuid())
				.name(project.getName())
				.entityType(DescribingEntityChange.EntityType.Project)
				.changeType(DescribingEntityChange.ChangeType.Saved)
				.changedProperties(changedProperties)
				.build());
	}

}
