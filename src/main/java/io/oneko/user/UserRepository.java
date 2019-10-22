package io.oneko.user;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Pretty much a persistent collection of users
 */
public interface UserRepository {

	Mono<User> getById(UUID userId);

	Mono<User> getByUserName(String userName);

	Mono<User> getByUserEmail(String userEmail);

	Flux<User> getAll();

	/**
	 * Persists the user.
	 */
	Mono<User> add(User user);

	Mono<Void> removeUser(User user);
}
