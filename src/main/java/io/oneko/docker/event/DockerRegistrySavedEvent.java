package io.oneko.docker.event;

import java.util.Collection;

import io.oneko.docker.DockerRegistry;
import io.oneko.docker.WritableDockerRegistry;
import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.event.EventTrigger;

public class DockerRegistrySavedEvent extends EntityChangedEvent {

	/**
	 * Use this constructor with the DockerRegistry object prior to actually saving it.
	 */
	public DockerRegistrySavedEvent(WritableDockerRegistry dockerRegistry, EventTrigger trigger) {
		this(dockerRegistry, dockerRegistry.getDirtyProperties(), trigger);
	}

	DockerRegistrySavedEvent(DockerRegistry dockerRegistry, Collection<String> changedProperties, EventTrigger trigger) {
		super(trigger, DescribingEntityChange.builder()
				.id(dockerRegistry.getUuid())
				.name(dockerRegistry.getName())
				.entityType(DescribingEntityChange.EntityType.DockerRegistry)
				.changeType(DescribingEntityChange.ChangeType.Saved)
				.changedProperties(changedProperties)
				.build());
	}


}
