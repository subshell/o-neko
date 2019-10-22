package io.oneko.user.auth;

import java.util.UUID;

import io.oneko.user.User;

/**
 * An abstraction of authentications for user objects with an identifier.
 *
 * @param <T> the identifier type.
 */
public interface UserAuthentication<T> {

	/**
	 * Provides the name of the user, this authentication refers to.
	 */
	UUID getUserId();

	/**
	 * Returns the user, this authentication instance refers to.
	 */
	User getUser();

	Class<T> getIdentifierType();

	/**
	 * Checks, whether the given  identifier is a valid authentication for this instances user.
	 *
	 * @param identifier
	 * @return
	 */
	boolean authenticates(T identifier);
}
