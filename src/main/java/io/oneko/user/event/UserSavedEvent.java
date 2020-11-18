package io.oneko.user.event;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.user.User;
import io.oneko.user.WritableUser;

import java.util.Collection;

public class UserSavedEvent extends EntityChangedEvent {

	/**
	 * Use this constructor with the user object prior to actually saving it.
	 */
	public UserSavedEvent(WritableUser user) {
		this(user, user.getDirtyProperties());
	}

	public UserSavedEvent(User user, Collection<String> changedProperties) {
		super(DescribingEntityChange.builder()
				.id(user.getUuid())
				.name(user.getUserName())
				.entityType(DescribingEntityChange.EntityType.User)
				.changeType(DescribingEntityChange.ChangeType.Saved)
				.changedProperties(changedProperties)
				.build());
	}
}
