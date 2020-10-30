package io.oneko.docker.event;

import io.oneko.docker.DockerRegistry;
import io.oneko.docker.DockerRegistryRepository;
import io.oneko.docker.ReadableDockerRegistry;
import io.oneko.docker.WritableDockerRegistry;
import io.oneko.event.EventDispatcher;

public abstract class EventAwareDockerRegistryRepository implements DockerRegistryRepository {

	private final EventDispatcher eventDispatcher;

	protected EventAwareDockerRegistryRepository(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public ReadableDockerRegistry add(WritableDockerRegistry dockerRegistry) {
		if (dockerRegistry.isDirty()) {
			ReadableDockerRegistry persistedRegistry = addInternally(dockerRegistry);
			this.eventDispatcher.dispatch(new DockerRegistrySavedEvent(dockerRegistry, null));//TODO
			return persistedRegistry;
		} else {
			return dockerRegistry.readable();
		}
	}

	protected abstract ReadableDockerRegistry addInternally(WritableDockerRegistry dockerRegistry);

	@Override
	public void remove(DockerRegistry dockerRegistry) {
		removeInternally(dockerRegistry);
		this.eventDispatcher.dispatch(new DockerRegistryDeletedEvent(dockerRegistry, null));//TODO
	}

	protected abstract void removeInternally(DockerRegistry dockerRegistry);
}
