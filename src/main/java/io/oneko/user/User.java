package io.oneko.user;

import io.oneko.security.UserRole;
import io.oneko.user.auth.UserAuthentication;

import java.util.UUID;

/**
 * User domain object. Contains all logic for doing whatever users need to do.<br/>
 * Comes with two implementations:
 * <ul>
 *     <li>{@link ReadableUser}</li>
 *     <li>{@link WritableUser}</li>
 * </ul>
 */
public interface User {

	default UUID getId() {
		return this.getUuid();
	}

	UUID getUuid();

	/**
	 * Checks, whether this user can be authorized by the given identifier against any of it's authentications.
	 */
	default <T> boolean authenticates(T identifier) {
		if (getAuthentication().getIdentifierType().isInstance(identifier)) {
			return ((UserAuthentication<T>) getAuthentication()).authenticates(identifier);
		}
		return false;
	}

	UserAuthentication<?> getAuthentication();

	String getUserName();

	String getFirstName();

	String getLastName();

	String getEmail();

	UserRole getRole();

}
