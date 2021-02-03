package io.oneko.helm.event;

import java.util.Collections;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.helm.HelmRegistry;

public class HelmRegistryDeletedEvent extends EntityChangedEvent {

	public HelmRegistryDeletedEvent(HelmRegistry helmRegistry) {
		super(DescribingEntityChange.builder()
				.id(helmRegistry.getId())
				.name(helmRegistry.getName())
				.entityType(DescribingEntityChange.EntityType.HelmRegistry)
				.changeType(DescribingEntityChange.ChangeType.Deleted)
				.changedProperties(Collections.emptySet())
				.build());
	}

}
