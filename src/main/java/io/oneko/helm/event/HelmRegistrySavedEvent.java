package io.oneko.helm.event;

import java.util.Collection;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.helm.HelmRegistry;
import io.oneko.helm.WritableHelmRegistry;

public class HelmRegistrySavedEvent extends EntityChangedEvent {

	/**
	 * Use this constructor with the HelmRegistry object prior to actually saving it.
	 */
	public HelmRegistrySavedEvent(WritableHelmRegistry helmRegistry) {
		this(helmRegistry, helmRegistry.getDirtyProperties());
	}

	HelmRegistrySavedEvent(HelmRegistry helmRegistry, Collection<String> changedProperties) {
		super(DescribingEntityChange.builder()
				.id(helmRegistry.getId())
				.name(helmRegistry.getName())
				.entityType(DescribingEntityChange.EntityType.HelmRegistry)
				.changeType(DescribingEntityChange.ChangeType.Saved)
				.changedProperties(changedProperties)
				.build());
	}

}
