package io.oneko.user.event;

import java.util.Collection;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.event.EntityChangedEvent;
import io.oneko.user.User;
import io.oneko.user.WritableUser;

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
