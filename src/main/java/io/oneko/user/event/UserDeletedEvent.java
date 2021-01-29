package io.oneko.user.event;

import java.util.Collections;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.user.User;

public class UserDeletedEvent extends EntityChangedEvent {

	public UserDeletedEvent(User user) {
		super(DescribingEntityChange.builder()
				.id(user.getUuid())
				.name(user.getUserName())
				.entityType(DescribingEntityChange.EntityType.User)
				.changeType(DescribingEntityChange.ChangeType.Deleted)
				.changedProperties(Collections.emptySet())
				.build());
	}

}
