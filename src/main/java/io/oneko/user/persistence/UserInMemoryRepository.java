package io.oneko.user.persistence;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.user.ReadableUser;
import io.oneko.user.User;
import io.oneko.user.WritableUser;
import io.oneko.user.event.EventAwareUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Profile(Profiles.IN_MEMORY)
public class UserInMemoryRepository extends EventAwareUserRepository {

	private final Map<UUID, ReadableUser> innerRepository = new HashMap<>();

	@Autowired
	public UserInMemoryRepository(EventDispatcher eventDispatcher) {
		super(eventDispatcher);
	}

	@Override
	protected ReadableUser addInternally(WritableUser user) {
		final ReadableUser readable = user.readable();
		this.innerRepository.put(readable.getId(), readable);
		return readable;
	}

	@Override
	protected void removeInternally(User user) {
		innerRepository.remove(user.getId());
	}

	@Override
	public Optional<ReadableUser> getById(UUID userId) {
		return Optional.ofNullable(innerRepository.get(userId));
	}

	@Override
	public Optional<ReadableUser> getByUserName(String userName) {
		return innerRepository.values().stream()
				.filter(user -> user.getUserName().equals(userName))
				.findFirst();
	}

	@Override
	public Optional<ReadableUser> getByUserEmail(String userEmail) {
		return innerRepository.values().stream()
				.filter(user -> user.getEmail().equals(userEmail))
				.findFirst();
	}

	@Override
	public List<ReadableUser> getAll() {
		return new ArrayList<>(innerRepository.values());
	}
}
