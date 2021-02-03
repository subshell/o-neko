package io.oneko.project.event;

import java.util.Collection;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.project.WritableProject;

public class ProjectSavedEvent extends EntityChangedEvent {

	/**
	 * Use this constructor only with the project prior to actually saving it.
	 */
	public ProjectSavedEvent(WritableProject project) {
		this(project, project.getDirtyProperties());
	}

	public ProjectSavedEvent(WritableProject project, Collection<String> changedProperties) {
		super(DescribingEntityChange.builder()
				.id(project.getId())
				.name(project.getName())
				.entityType(DescribingEntityChange.EntityType.Project)
				.changeType(DescribingEntityChange.ChangeType.Saved)
				.changedProperties(changedProperties)
				.build());
	}

}
