package io.oneko.namespace.event;

import io.oneko.event.EventDispatcher;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.DefinedNamespaceRepository;
import io.oneko.namespace.ReadableDefinedNamespace;
import io.oneko.namespace.WritableDefinedNamespace;
import reactor.core.publisher.Mono;


public abstract class EventAwareDefinedNamespaceRepository implements DefinedNamespaceRepository {

	private final EventDispatcher eventDispatcher;

	protected EventAwareDefinedNamespaceRepository(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Mono<ReadableDefinedNamespace> add(WritableDefinedNamespace namespace) {
		if (namespace.isDirty()) {
			Mono<ReadableDefinedNamespace> namespaceMono = addInternally(namespace);
			return this.eventDispatcher.createAndDispatchEvent(namespaceMono, (u, t) -> new DefinedNamespaceSavedEvent(namespace, t));
		} else {
			return Mono.just(namespace.readable());
		}
	}

	protected abstract Mono<ReadableDefinedNamespace> addInternally(WritableDefinedNamespace namespace);

	@Override
	public Mono<Void> remove(DefinedNamespace namespace) {
		Mono<Void> voidMono = removeInternally(namespace);
		return this.eventDispatcher.createAndDispatchEvent(voidMono, (v, trigger) -> new DefinedNamespaceDeletedEvent(namespace, trigger));
	}

	protected abstract Mono<Void> removeInternally(DefinedNamespace namespace);
}
