package io.oneko.user.event;

import io.oneko.event.EventDispatcher;
import io.oneko.user.ReadableUser;
import io.oneko.user.User;
import io.oneko.user.UserRepository;
import io.oneko.user.WritableUser;

public abstract class EventAwareUserRepository implements UserRepository {

	private final EventDispatcher eventDispatcher;

	protected EventAwareUserRepository(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public ReadableUser add(WritableUser user) {
		if (user.isDirty()) {
			ReadableUser persistedUser = addInternally(user);
			eventDispatcher.dispatch(new UserSavedEvent(user));
			return persistedUser;
		} else {
			return user.readable();
		}
	}

	protected abstract ReadableUser addInternally(WritableUser user);

	@Override
	public void removeUser(User user) {
		removeInternally(user);
		eventDispatcher.dispatch(new UserDeletedEvent(user));
	}

	protected abstract void removeInternally(User user);
}
