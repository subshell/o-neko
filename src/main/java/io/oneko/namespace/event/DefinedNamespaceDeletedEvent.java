package io.oneko.namespace.event;

import java.util.Collections;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.event.EventTrigger;
import io.oneko.namespace.DefinedNamespace;

public class DefinedNamespaceDeletedEvent extends EntityChangedEvent {

	public DefinedNamespaceDeletedEvent(DefinedNamespace namespace) {
		super(DescribingEntityChange.builder()
				.id(namespace.getId())
				.name(namespace.asKubernetesNameSpace())
				.entityType(DescribingEntityChange.EntityType.Namespace)
				.changeType(DescribingEntityChange.ChangeType.Deleted)
				.changedProperties(Collections.emptySet())
				.build());
	}
}
