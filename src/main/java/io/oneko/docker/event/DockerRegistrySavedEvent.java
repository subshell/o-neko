package io.oneko.docker.event;

import java.util.Collection;

import io.oneko.docker.DockerRegistry;
import io.oneko.docker.WritableDockerRegistry;
import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;

public class DockerRegistrySavedEvent extends EntityChangedEvent {

	/**
	 * Use this constructor with the HelmRegistry object prior to actually saving it.
	 */
	public DockerRegistrySavedEvent(WritableDockerRegistry dockerRegistry) {
		this(dockerRegistry, dockerRegistry.getDirtyProperties());
	}

	DockerRegistrySavedEvent(DockerRegistry dockerRegistry, Collection<String> changedProperties) {
		super(DescribingEntityChange.builder()
				.id(dockerRegistry.getUuid())
				.name(dockerRegistry.getName())
				.entityType(DescribingEntityChange.EntityType.HelmRegistry)
				.changeType(DescribingEntityChange.ChangeType.Saved)
				.changedProperties(changedProperties)
				.build());
	}
}
