package io.oneko.projectmesh.event;

import io.oneko.event.EventDispatcher;
import io.oneko.projectmesh.ProjectMesh;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableProjectMesh;
import io.oneko.projectmesh.ProjectMeshRepository;

public abstract class EventAwareProjectMeshRepository implements ProjectMeshRepository {

	private final EventDispatcher eventDispatcher;

	protected EventAwareProjectMeshRepository(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public ReadableProjectMesh add(WritableProjectMesh mesh) {
		if (mesh.isDirty()) {
			ReadableProjectMesh persisted = addInternally(mesh);
			this.eventDispatcher.dispatch(new ProjectMeshSavedEvent(mesh, null));//TODO
			return persisted;
		} else {
			return mesh.readable();
		}
	}

	protected abstract ReadableProjectMesh addInternally(WritableProjectMesh mesh);

	@Override
	public void remove(ProjectMesh<?, ?> mesh) {
		removeInternally(mesh);
		eventDispatcher.dispatch(new ProjectMeshDeletedEvent(mesh, null));//TODO
	}

	protected abstract void removeInternally(ProjectMesh<?, ?> mesh);
}
