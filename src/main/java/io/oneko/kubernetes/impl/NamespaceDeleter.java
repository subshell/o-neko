package io.oneko.kubernetes.impl;

import io.oneko.docker.event.ObsoleteProjectVersionRemovedEvent;
import io.oneko.event.Event;
import io.oneko.event.EventDispatcher;
import io.oneko.project.event.ProjectDeletedEvent;
import org.springframework.stereotype.Service;

@Service
public class NamespaceDeleter {

	private final KubernetesAccess kubernetesAccess;

	public NamespaceDeleter(KubernetesAccess kubernetesAccess, EventDispatcher eventDispatcher) {
		this.kubernetesAccess = kubernetesAccess;
		eventDispatcher.streamEvents().subscribe(this::processEvent);
	}

	public void processEvent(Event event) {
		if (event instanceof ProjectDeletedEvent) {
			ProjectDeletedEvent pde = (ProjectDeletedEvent) event;
			kubernetesAccess.deleteNamespacesWithProjectId(pde.describeEntityChange().getId().toString());
		}

		if (event instanceof ObsoleteProjectVersionRemovedEvent) {
			ObsoleteProjectVersionRemovedEvent opvre = (ObsoleteProjectVersionRemovedEvent) event;
			kubernetesAccess.deleteNamespaceWithProjectVersionId(opvre.getVersionId().toString());
		}
	}
}
