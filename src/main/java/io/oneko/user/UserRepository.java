package io.oneko.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Pretty much a persistent collection of users
 */
public interface UserRepository {

	Optional<ReadableUser> getById(UUID userId);

	Optional<ReadableUser> getByUserName(String userName);

	Optional<ReadableUser> getByUserEmail(String userEmail);

	List<ReadableUser> getAll();

	/**
	 * Persists the user.
	 */
	ReadableUser add(WritableUser user);

	void removeUser(User user);
}
