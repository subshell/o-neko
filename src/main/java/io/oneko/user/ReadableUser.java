package io.oneko.user;

import java.io.Serializable;
import java.util.UUID;

import io.oneko.domain.Identifiable;
import io.oneko.security.UserRole;
import io.oneko.user.auth.UserAuthentication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode(callSuper = true)
public class ReadableUser extends Identifiable implements User, Serializable {

	private final UUID uuid;
	private final String userName;
	private final String firstName;
	private final String lastName;
	private final String email;
	private final UserRole role;
	private final transient UserAuthentication<?> authentication;

	@Override
	public UUID getId() {
		return uuid;
	}

	/**
	 * Checks, whether this user can be authorized by the given identifier against any of it's authentications.
	 */
	public <T> boolean authenticates(T identifier) {
		if (authentication.getIdentifierType().isInstance(identifier)) {
			return ((UserAuthentication<T>) this.authentication).authenticates(identifier);
		}
		return false;
	}

	public WritableUser writable() {
		return new WritableUser(this.getUuid(), this.getUserName(), this.getFirstName(), this.getLastName(), this.getEmail(), this.getRole(), this.getAuthentication());
	}
}
