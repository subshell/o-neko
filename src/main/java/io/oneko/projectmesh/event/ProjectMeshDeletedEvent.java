package io.oneko.projectmesh.event;

import java.util.Collections;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.event.EventTrigger;
import io.oneko.projectmesh.ProjectMesh;
import io.oneko.projectmesh.WritableProjectMesh;

public class ProjectMeshDeletedEvent extends EntityChangedEvent {

	public ProjectMeshDeletedEvent(ProjectMesh<?, ?> mesh, EventTrigger trigger) {
		super(trigger, DescribingEntityChange.builder()
				.id(mesh.getId())
				.name(mesh.getName())
				.entityType(DescribingEntityChange.EntityType.ProjectMesh)
				.changeType(DescribingEntityChange.ChangeType.Deleted)
				.changedProperties(Collections.emptySet())
				.build());
	}
}
