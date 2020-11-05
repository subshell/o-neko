package io.oneko.project.event;

import io.oneko.event.EventDispatcher;
import io.oneko.project.Project;
import io.oneko.project.ReadableProject;
import io.oneko.project.WritableProject;
import io.oneko.project.ProjectRepository;

public abstract class EventAwareProjectRepository implements ProjectRepository {

	private final EventDispatcher eventDispatcher;

	protected EventAwareProjectRepository(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public ReadableProject add(WritableProject project) {
		if (project.isDirty()) {
			ReadableProject persistedProject = addInternally(project);
			this.eventDispatcher.dispatch(new ProjectSavedEvent(project));
			return persistedProject;
		} else {
			return project.readable();
		}
	}

	protected abstract ReadableProject addInternally(WritableProject project);

	@Override
	public void remove(Project<?, ?> project) {
		removeInternally(project);
		eventDispatcher.dispatch(new ProjectDeletedEvent(project));
	}

	protected abstract void removeInternally(Project<?, ?> project);
}
