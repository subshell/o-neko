package io.oneko.kubernetes.impl;

import java.util.function.Consumer;

import javax.annotation.PreDestroy;

import io.oneko.docker.event.ObsoleteProjectVersionRemovedEvent;
import io.oneko.event.Event;
import io.oneko.event.EventDispatcher;
import io.oneko.project.event.ProjectDeletedEvent;
import org.springframework.stereotype.Service;

@Service
public class NamespaceDeleter {

	private final KubernetesAccess kubernetesAccess;
	private final EventDispatcher eventDispatcher;
	private final Consumer<Event> eventListener = this::processEvent;


	public NamespaceDeleter(KubernetesAccess kubernetesAccess, EventDispatcher eventDispatcher) {
		this.kubernetesAccess = kubernetesAccess;
		this.eventDispatcher = eventDispatcher;
		eventDispatcher.registerListener(eventListener);
	}

	@PreDestroy
	public void cleanup() {
		eventDispatcher.removeListener(eventListener);
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
