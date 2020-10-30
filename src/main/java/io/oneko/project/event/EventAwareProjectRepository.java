package io.oneko.project.event;

import io.oneko.event.EventDispatcher;
import io.oneko.project.Project;
import io.oneko.project.ReadableProject;
import io.oneko.project.WritableProject;
import io.oneko.project.ProjectRepository;
import reactor.core.publisher.Mono;

public abstract class EventAwareProjectRepository implements ProjectRepository {

	private final EventDispatcher eventDispatcher;

	protected EventAwareProjectRepository(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Mono<ReadableProject> add(WritableProject project) {
		if (project.isDirty()) {
			Mono<ReadableProject> projectMono = addInternally(project);
			// we use the project as before it is persisted to have its dirty properties available for the event.
			return this.eventDispatcher.createAndDispatchEvent(projectMono, (p, t) -> new ProjectSavedEvent(project, t));
		} else {
			return Mono.just(project.readable());
		}
	}

	protected abstract Mono<ReadableProject> addInternally(WritableProject project);

	private void dispatchProjectDeletedEvent(Project<?, ?> project) {
		eventDispatcher.createAndDispatchEvent((trigger) -> new ProjectDeletedEvent(project, trigger));
	}

	@Override
	public Mono<Void> remove(Project<?, ?> project) {
		Mono<Void> voidMono = removeInternally(project);
		dispatchProjectDeletedEvent(project);
		return voidMono;
	}

	protected abstract Mono<Void> removeInternally(Project<?, ?> project);
}
