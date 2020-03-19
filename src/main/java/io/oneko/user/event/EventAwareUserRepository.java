package io.oneko.user.event;

import io.oneko.event.EventDispatcher;
import io.oneko.user.ReadableUser;
import io.oneko.user.User;
import io.oneko.user.UserRepository;
import io.oneko.user.WritableUser;
import reactor.core.publisher.Mono;

public abstract class EventAwareUserRepository implements UserRepository {

	private final EventDispatcher eventDispatcher;

	protected EventAwareUserRepository(EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Mono<ReadableUser> add(WritableUser user) {
		if (user.isDirty()) {
			Mono<ReadableUser> userMono = addInternally(user);
			return this.eventDispatcher.createAndDispatchEvent(userMono, (u, t) -> new UserSavedEvent(user, t));
		} else {
			return Mono.just(user.readable());
		}
	}

	protected abstract Mono<ReadableUser> addInternally(WritableUser user);

	@Override
	public Mono<Void> removeUser(User user) {
		Mono<Void> voidMono = removeInternally(user);
		return this.eventDispatcher.createAndDispatchEvent(voidMono, (v, trigger) -> new UserDeletedEvent(user, trigger));
	}

	protected abstract Mono<Void> removeInternally(User user);
}
