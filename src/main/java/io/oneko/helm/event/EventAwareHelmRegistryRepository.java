package io.oneko.helm.event;

import io.oneko.event.EventDispatcher;
import io.oneko.helm.HelmRegistry;
import io.oneko.helm.HelmRegistryRepository;
import io.oneko.helm.ReadableHelmRegistry;
import io.oneko.helm.WritableHelmRegistry;

public abstract class EventAwareHelmRegistryRepository implements HelmRegistryRepository {

	private final EventDispatcher eventDispatcher;

	protected EventAwareHelmRegistryRepository(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public ReadableHelmRegistry add(WritableHelmRegistry helmRegistry) {
		if (helmRegistry.isDirty()) {
			ReadableHelmRegistry persistedRegistry = addInternally(helmRegistry);
			this.eventDispatcher.dispatch(new HelmRegistrySavedEvent(helmRegistry));
			return persistedRegistry;
		}

		return helmRegistry.readable();
	}

	protected abstract ReadableHelmRegistry addInternally(WritableHelmRegistry helmRegistry);

	@Override
	public void remove(HelmRegistry helmRegistry) {
		removeInternally(helmRegistry);
		this.eventDispatcher.dispatch(new HelmRegistryDeletedEvent(helmRegistry));
	}

	protected abstract void removeInternally(HelmRegistry helmRegistry);
}
