package io.oneko.namespace.event;

import io.oneko.event.EventDispatcher;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.DefinedNamespaceRepository;
import io.oneko.namespace.ReadableDefinedNamespace;
import io.oneko.namespace.WritableDefinedNamespace;

public abstract class EventAwareDefinedNamespaceRepository implements DefinedNamespaceRepository {

	private final EventDispatcher eventDispatcher;

	protected EventAwareDefinedNamespaceRepository(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public ReadableDefinedNamespace add(WritableDefinedNamespace namespace) {
		if (namespace.isDirty()) {
			ReadableDefinedNamespace persistedNameSpace = addInternally(namespace);
			eventDispatcher.dispatch(new DefinedNamespaceSavedEvent(namespace));
			return persistedNameSpace;
		} else {
			return namespace.readable();
		}
	}

	protected abstract ReadableDefinedNamespace addInternally(WritableDefinedNamespace namespace);

	@Override
	public void remove(DefinedNamespace namespace) {
		removeInternally(namespace);
		this.eventDispatcher.dispatch(new DefinedNamespaceDeletedEvent(namespace));
	}

	protected abstract void removeInternally(DefinedNamespace namespace);
}
