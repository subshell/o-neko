package io.oneko.project.event;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.project.Project;

import java.util.Collections;

public class ProjectDeletedEvent extends EntityChangedEvent {

	public ProjectDeletedEvent(Project project) {
		super(DescribingEntityChange.builder()
				.id(project.getId())
				.name(project.getName())
				.entityType(DescribingEntityChange.EntityType.Project)
				.changeType(DescribingEntityChange.ChangeType.Deleted)
				.changedProperties(Collections.emptySet())
				.build());
	}
}
