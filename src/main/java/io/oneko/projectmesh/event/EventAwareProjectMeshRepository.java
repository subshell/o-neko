package io.oneko.projectmesh.event;

import io.oneko.event.EventDispatcher;
import io.oneko.projectmesh.ProjectMesh;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableProjectMesh;
import io.oneko.projectmesh.ProjectMeshRepository;
import reactor.core.publisher.Mono;

public abstract class EventAwareProjectMeshRepository implements ProjectMeshRepository {

	private final EventDispatcher eventDispatcher;

	protected EventAwareProjectMeshRepository(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Mono<ReadableProjectMesh> add(WritableProjectMesh mesh) {
		if (mesh.isDirty()) {
			Mono<ReadableProjectMesh> meshMono = addInternally(mesh);
			// we use the mesh as before it is persisted to have its dirty properties available for the event.
			return this.eventDispatcher.createAndDispatchEvent(meshMono, (p, t) -> new ProjectMeshSavedEvent(mesh, t));
		} else {
			return Mono.just(mesh.readable());
		}
	}

	protected abstract Mono<ReadableProjectMesh> addInternally(WritableProjectMesh mesh);

	private void dispatchProjectDeletedEvent(ProjectMesh<?, ?> mesh) {
		eventDispatcher.createAndDispatchEvent((trigger) -> new ProjectMeshDeletedEvent(mesh, trigger));
	}

	@Override
	public Mono<Void> remove(ProjectMesh<?, ?> mesh) {
		Mono<Void> voidMono = removeInternally(mesh);
		dispatchProjectDeletedEvent(mesh);
		return voidMono;
	}

	protected abstract Mono<Void> removeInternally(ProjectMesh<?, ?> mesh);
}
