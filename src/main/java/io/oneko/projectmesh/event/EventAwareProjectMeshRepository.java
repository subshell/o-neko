package io.oneko.projectmesh.event;

import io.oneko.event.EventDispatcher;
import io.oneko.projectmesh.ProjectMesh;
import io.oneko.projectmesh.ProjectMeshRepository;
import reactor.core.publisher.Mono;

public abstract class EventAwareProjectMeshRepository implements ProjectMeshRepository {

	private final EventDispatcher eventDispatcher;

	protected EventAwareProjectMeshRepository(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Mono<ProjectMesh> add(ProjectMesh mesh) {
		if (mesh.isDirty()) {
			Mono<ProjectMesh> meshMono = addInternally(mesh);
			// we use the mesh as before it is persisted to have its dirty properties available for the event.
			return this.eventDispatcher.createAndDispatchEvent(meshMono, (p, t) -> new ProjectMeshSavedEvent(mesh, t));
		} else {
			return Mono.just(mesh);
		}
	}

	protected abstract Mono<ProjectMesh> addInternally(ProjectMesh mesh);

	private void dispatchProjectDeletedEvent(ProjectMesh mesh) {
		eventDispatcher.createAndDispatchEvent((trigger) -> new ProjectMeshDeletedEvent(mesh, trigger));
	}

	@Override
	public Mono<Void> remove(ProjectMesh mesh) {
		Mono<Void> voidMono = removeInternally(mesh);
		dispatchProjectDeletedEvent(mesh);
		return voidMono;
	}

	protected abstract Mono<Void> removeInternally(ProjectMesh mesh);
}
