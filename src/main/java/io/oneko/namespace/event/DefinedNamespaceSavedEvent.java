package io.oneko.namespace.event;

import java.util.Collection;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.event.EventTrigger;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.WritableDefinedNamespace;

public class DefinedNamespaceSavedEvent extends EntityChangedEvent {

	/**
	 * Use this constructor with the user object prior to actually saving it.
	 */
	public DefinedNamespaceSavedEvent(WritableDefinedNamespace namespace, EventTrigger trigger) {
		this(namespace, namespace.getDirtyProperties(), trigger);
	}

	public DefinedNamespaceSavedEvent(DefinedNamespace namespace, Collection<String> changedProperties, EventTrigger trigger) {
		super(trigger, DescribingEntityChange.builder()
				.id(namespace.getId())
				.name(namespace.asKubernetesNameSpace())
				.entityType(DescribingEntityChange.EntityType.Namespace)
				.changeType(DescribingEntityChange.ChangeType.Saved)
				.changedProperties(changedProperties)
				.build());
	}
}
