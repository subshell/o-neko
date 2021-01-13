package io.oneko.projectmesh.event;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.projectmesh.WritableProjectMesh;

import java.util.Collection;

public class ProjectMeshSavedEvent extends EntityChangedEvent {

	/**
	 * Use this constructor only with the mesh prior to actually saving it.
	 */
	public ProjectMeshSavedEvent(WritableProjectMesh mesh) {
		this(mesh, mesh.getDirtyProperties());
	}

	public ProjectMeshSavedEvent(WritableProjectMesh mesh, Collection<String> changedProperties) {
		super(DescribingEntityChange.builder()
				.id(mesh.getId())
				.name(mesh.getName())
				.entityType(DescribingEntityChange.EntityType.ProjectMesh)
				.changeType(DescribingEntityChange.ChangeType.Saved)
				.changedProperties(changedProperties)
				.build());
	}
}
