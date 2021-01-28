package io.oneko.namespace.event;

import io.oneko.event.EventDispatcher;
import io.oneko.namespace.Namespace;
import io.oneko.namespace.NamespaceRepository;
import io.oneko.namespace.ReadableNamespace;
import io.oneko.namespace.WritableNamespace;

public abstract class EventAwareNamespaceRepository implements NamespaceRepository {

	private final EventDispatcher eventDispatcher;

	protected EventAwareNamespaceRepository(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public ReadableNamespace add(WritableNamespace namespace) {
		if (namespace.isDirty()) {
			ReadableNamespace persistedNameSpace = addInternally(namespace);
			eventDispatcher.dispatch(new DefinedNamespaceSavedEvent(namespace));
			return persistedNameSpace;
		} else {
			return namespace.readable();
		}
	}

	protected abstract ReadableNamespace addInternally(WritableNamespace namespace);

	@Override
	public void remove(Namespace namespace) {
		removeInternally(namespace);
		this.eventDispatcher.dispatch(new DefinedNamespaceDeletedEvent(namespace));
	}

	protected abstract void removeInternally(Namespace namespace);
}
