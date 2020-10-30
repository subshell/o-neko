package io.oneko.docker.event;

import io.oneko.docker.DockerRegistry;
import io.oneko.docker.DockerRegistryRepository;
import io.oneko.docker.ReadableDockerRegistry;
import io.oneko.docker.WritableDockerRegistry;
import io.oneko.event.EventDispatcher;
import reactor.core.publisher.Mono;

public abstract class EventAwareDockerRegistryRepository implements DockerRegistryRepository {

	private final EventDispatcher eventDispatcher;

	protected EventAwareDockerRegistryRepository(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Mono<ReadableDockerRegistry> add(WritableDockerRegistry dockerRegistry) {
		if (dockerRegistry.isDirty()) {
			Mono<ReadableDockerRegistry> dockerRegistryMono = addInternally(dockerRegistry);
			return this.eventDispatcher.createAndDispatchEvent(dockerRegistryMono, (d, t) -> new DockerRegistrySavedEvent(dockerRegistry, t));
		} else {
			return Mono.just(dockerRegistry.readable());
		}
	}

	protected abstract Mono<ReadableDockerRegistry> addInternally(WritableDockerRegistry dockerRegistry);

	@Override
	public Mono<Void> remove(DockerRegistry dockerRegistry) {
		Mono<Void> voidMono = removeInternally(dockerRegistry);
		return this.eventDispatcher.createAndDispatchEvent(voidMono, (v, trigger) -> new DockerRegistryDeletedEvent(dockerRegistry, trigger));
	}

	protected abstract Mono<Void> removeInternally(DockerRegistry dockerRegistry);
}
