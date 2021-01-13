package io.oneko.projectmesh.event;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.projectmesh.ProjectMesh;

import java.util.Collections;

public class ProjectMeshDeletedEvent extends EntityChangedEvent {

	public ProjectMeshDeletedEvent(ProjectMesh<?, ?> mesh) {
		super(DescribingEntityChange.builder()
				.id(mesh.getId())
				.name(mesh.getName())
				.entityType(DescribingEntityChange.EntityType.ProjectMesh)
				.changeType(DescribingEntityChange.ChangeType.Deleted)
				.changedProperties(Collections.emptySet())
				.build());
	}
}
