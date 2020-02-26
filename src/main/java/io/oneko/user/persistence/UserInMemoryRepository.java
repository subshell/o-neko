package io.oneko.user.persistence;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.user.User;
import io.oneko.user.WritableUser;
import io.oneko.user.event.EventAwareUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Profile(Profiles.IN_MEMORY)
public class UserInMemoryRepository extends EventAwareUserRepository {

	private final Map<UUID, User> innerRepository = new HashMap<>();

	@Autowired
	UserInMemoryRepository(EventDispatcher eventDispatcher) {
		super(eventDispatcher);
	}

	@Override
	protected Mono<User> addInternally(WritableUser user) {
		final User readable = user.readable();
		this.innerRepository.put(readable.getId(), readable);
		return Mono.just(readable);
	}

	@Override
	protected Mono<Void> removeInternally(User user) {
		innerRepository.remove(user.getId());
		return Mono.empty();
	}

	@Override
	public Mono<User> getById(UUID userId) {
		return Mono.justOrEmpty(innerRepository.get(userId));
	}

	@Override
	public Mono<User> getByUserName(String userName) {
		return Mono.justOrEmpty(innerRepository.values().stream()
				.filter(user -> user.getUserName().equals(userName))
				.findFirst());
	}

	@Override
	public Mono<User> getByUserEmail(String userEmail) {
		return Mono.justOrEmpty(innerRepository.values().stream()
				.filter(user -> user.getEmail().equals(userEmail))
				.findFirst());
	}

	@Override
	public Flux<User> getAll() {
		return Flux.fromIterable(innerRepository.values());
	}
}
