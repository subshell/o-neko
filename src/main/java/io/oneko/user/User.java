package io.oneko.user;

import java.io.Serializable;
import java.util.UUID;

import io.oneko.domain.Identifiable;
import io.oneko.domain.ModificationAwareContainer;

import io.oneko.domain.ModificationAwareProperty;
import io.oneko.security.UserRole;
import io.oneko.user.auth.UserAuthentication;
import lombok.Builder;

/**
 * User domain object. Contains all logic for doing whatever users need to do.
 */
public class User extends Identifiable implements Serializable {

	protected final ModificationAwareContainer modifications = new ModificationAwareContainer();
	protected final ModificationAwareProperty<UUID> uuid = new ModificationAwareProperty<>(modifications, "uuid");
	protected final ModificationAwareProperty<String> userName = new ModificationAwareProperty<>(modifications, "userName");
	protected final ModificationAwareProperty<String> firstName = new ModificationAwareProperty<>(modifications, "firstName");
	protected final ModificationAwareProperty<String> lastName = new ModificationAwareProperty<>(modifications, "lastName");
	protected final ModificationAwareProperty<String> email = new ModificationAwareProperty<>(modifications, "email");
	protected final ModificationAwareProperty<UserRole> role = new ModificationAwareProperty<>(modifications, "role");

	transient protected ModificationAwareProperty<UserAuthentication<?>> authentication = new ModificationAwareProperty<>(modifications, "authentication");

	/**
	 * Creates a completely new user.
	 */
	protected User() {
		this.uuid.set(UUID.randomUUID());
	}

	@Builder
	public User(UUID uuid, String userName, String firstName, String lastName, String email, UserRole role, UserAuthentication<?> authentication) {
		this.uuid.init(uuid);
		this.userName.init(userName);
		this.firstName.init(firstName);
		this.lastName.init(lastName);
		this.email.init(email);
		this.role.init(role);
		this.authentication.init(authentication);
	}

	@Override
	public UUID getId() {
		return this.uuid.get();
	}

	public UUID getUuid() {
		return uuid.get();
	}

	//add methods for creating LDAP authentications or similar foo here:

	/**
	 * Checks, whether this user can be authorized by the given identifier against any of it's authentications.
	 */
	public <T> boolean authenticates(T identifier) {
		if (this.authentication.get().getIdentifierType().isInstance(identifier)) {
			return ((UserAuthentication<T>) this.authentication).authenticates(identifier);
		}
		return false;
	}

	public UserAuthentication<?> getAuthentication() {
		return authentication.get();
	}

	public String getUserName() {
		return userName.get();
	}

	public String getFirstName() {
		return firstName.get();
	}

	public String getLastName() {
		return lastName.get();
	}

	public String getEmail() {
		return email.get();
	}

	public UserRole getRole() {
		return role.get();
	}

	public WritableUser writable() {
		return new WritableUser(this.getUuid(), this.getUserName(), this.getFirstName(), this.getLastName(), this.getEmail(), this.getRole(), this.getAuthentication());
	}
}
