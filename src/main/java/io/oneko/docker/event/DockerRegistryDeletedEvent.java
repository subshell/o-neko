package io.oneko.docker.event;

import io.oneko.docker.DockerRegistry;
import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;

import java.util.Collections;

public class DockerRegistryDeletedEvent extends EntityChangedEvent {

	public DockerRegistryDeletedEvent(DockerRegistry dockerRegistry) {
		super(DescribingEntityChange.builder()
				.id(dockerRegistry.getUuid())
				.name(dockerRegistry.getName())
				.entityType(DescribingEntityChange.EntityType.DockerRegistry)
				.changeType(DescribingEntityChange.ChangeType.Deleted)
				.changedProperties(Collections.emptySet())
				.build());
	}

}
