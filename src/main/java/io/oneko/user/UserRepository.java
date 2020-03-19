package io.oneko.user;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Pretty much a persistent collection of users
 */
public interface UserRepository {

	Mono<ReadableUser> getById(UUID userId);

	Mono<ReadableUser> getByUserName(String userName);

	Mono<ReadableUser> getByUserEmail(String userEmail);

	Flux<ReadableUser> getAll();

	/**
	 * Persists the user.
	 */
	Mono<ReadableUser> add(WritableUser user);

	Mono<Void> removeUser(User user);
}
