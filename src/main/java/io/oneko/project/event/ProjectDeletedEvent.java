package io.oneko.project.event;

import java.util.Collections;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.event.EventTrigger;
import io.oneko.project.Project;
import io.oneko.project.WritableProject;

public class ProjectDeletedEvent extends EntityChangedEvent {

	public ProjectDeletedEvent(Project project, EventTrigger trigger) {
		super(trigger, DescribingEntityChange.builder()
				.id(project.getUuid())
				.name(project.getName())
				.entityType(DescribingEntityChange.EntityType.Project)
				.changeType(DescribingEntityChange.ChangeType.Deleted)
				.changedProperties(Collections.emptySet())
				.build());
	}
}
