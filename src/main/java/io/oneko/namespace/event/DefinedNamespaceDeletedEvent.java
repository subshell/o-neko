package io.oneko.namespace.event;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.namespace.DefinedNamespace;

import java.util.Collections;

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
