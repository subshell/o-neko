package io.oneko.user;

import java.io.Serializable;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;

import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareProperty;
import io.oneko.security.UserRole;
import io.oneko.user.auth.PasswordBasedUserAuthentication;
import io.oneko.user.auth.UserAuthentication;
import lombok.Builder;

/**
 * User domain object. Contains all logic for doing whatever users need to do.
 */
public class User extends ModificationAwareIdentifiable implements Serializable {

	private final ModificationAwareProperty<UUID> uuid = new ModificationAwareProperty<>(this, "uuid");
	private final ModificationAwareProperty<String> userName = new ModificationAwareProperty<>(this, "userName");
	private final ModificationAwareProperty<String> firstName = new ModificationAwareProperty<>(this, "firstName");
	private final ModificationAwareProperty<String> lastName = new ModificationAwareProperty<>(this, "lastName");
	private final ModificationAwareProperty<String> email = new ModificationAwareProperty<>(this, "email");
	private final ModificationAwareProperty<UserRole> role = new ModificationAwareProperty<>(this, "role");

	transient private ModificationAwareProperty<UserAuthentication<?>> authentication = new ModificationAwareProperty<>(this, "authentication");

	/**
	 * Creates a completely new user.
	 */
	public User() {
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

	/**
	 * Creates a new password authentication for this user.
	 *
	 * @param password The plain password to set as authentication for this user.
	 */
	public void setPasswordAuthentication(String password, PasswordEncoder passwordEncoder) {
		this.authentication.set(new PasswordBasedUserAuthentication(this.uuid.get(), () -> this, passwordEncoder.encode(password), passwordEncoder));
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

	public void setUserName(String username) {
		this.userName.set(username);
	}

	public String getFirstName() {
		return firstName.get();
	}

	public void setFirstName(String firstName) {
		this.firstName.set(firstName);
	}

	public String getLastName() {
		return lastName.get();
	}

	public void setLastName(String lastName) {
		this.lastName.set(lastName);
	}

	public String getEmail() {
		return email.get();
	}

	public void setEmail(String email) {
		this.email.set(email);
	}

	public UserRole getRole() {
		return role.get();
	}

	public void setRole(UserRole role) {
		this.role.set(role);
	}
}
