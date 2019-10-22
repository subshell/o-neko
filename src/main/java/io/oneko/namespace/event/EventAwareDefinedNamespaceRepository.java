package io.oneko.namespace.event;

import io.oneko.event.EventDispatcher;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.DefinedNamespaceRepository;
import reactor.core.publisher.Mono;


public abstract class EventAwareDefinedNamespaceRepository implements DefinedNamespaceRepository {

	private final EventDispatcher eventDispatcher;

	protected EventAwareDefinedNamespaceRepository(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Mono<DefinedNamespace> add(DefinedNamespace namespace) {
		if (namespace.isDirty()) {
			Mono<DefinedNamespace> namespaceMono = addInternally(namespace);
			return this.eventDispatcher.createAndDispatchEvent(namespaceMono, (u, t) -> new DefinedNamespaceSavedEvent(namespace, t));
		} else {
			return Mono.just(namespace);
		}
	}

	protected abstract Mono<DefinedNamespace> addInternally(DefinedNamespace namespace);

	@Override
	public Mono<Void> remove(DefinedNamespace namespace) {
		Mono<Void> voidMono = removeInternally(namespace);
		return this.eventDispatcher.createAndDispatchEvent(voidMono, (v, trigger) -> new DefinedNamespaceDeletedEvent(namespace, trigger));
	}

	protected abstract Mono<Void> removeInternally(DefinedNamespace namespace);
}
