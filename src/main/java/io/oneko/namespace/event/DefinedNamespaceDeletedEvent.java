package io.oneko.namespace.event;

import java.util.Collections;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.namespace.Namespace;

public class DefinedNamespaceDeletedEvent extends EntityChangedEvent {

	public DefinedNamespaceDeletedEvent(Namespace namespace) {
		super(DescribingEntityChange.builder()
				.id(namespace.getId())
				.name(namespace.asKubernetesNameSpace())
				.entityType(DescribingEntityChange.EntityType.Namespace)
				.changeType(DescribingEntityChange.ChangeType.Deleted)
				.changedProperties(Collections.emptySet())
				.build());
	}
}
