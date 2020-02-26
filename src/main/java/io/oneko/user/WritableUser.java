package io.oneko.user;

import io.oneko.domain.ModificationAware;
import io.oneko.security.UserRole;
import io.oneko.user.auth.PasswordBasedUserAuthentication;
import io.oneko.user.auth.UserAuthentication;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.UUID;

public class WritableUser extends User implements ModificationAware {

	/**
	 * Creates a completely new user.
	 */
	public WritableUser() {
		this.uuid.set(UUID.randomUUID());
	}

	public WritableUser(UUID uuid, String userName, String firstName, String lastName, String email, UserRole role, UserAuthentication<?> authentication) {
		super(uuid, userName, firstName, lastName, email, role, authentication);
	}

	/**
	 * Creates a new password authentication for this user.
	 *
	 * @param password The plain password to set as authentication for this user.
	 */
	public void setPasswordAuthentication(String password, PasswordEncoder passwordEncoder) {
		this.authentication.set(new PasswordBasedUserAuthentication(this.uuid.get(), () -> this, passwordEncoder.encode(password), passwordEncoder));
	}

	public void setUserName(String username) {
		this.userName.set(username);
	}

	public void setFirstName(String firstName) {
		this.firstName.set(firstName);
	}

	public void setLastName(String lastName) {
		this.lastName.set(lastName);
	}

	public void setEmail(String email) {
		this.email.set(email);
	}

	public void setRole(UserRole role) {
		this.role.set(role);
	}

	public User readable() {
		return new User(this.getUuid(), this.getUserName(), this.getFirstName(), this.getLastName(), this.getEmail(), this.getRole(), this.getAuthentication());
	}

	@Override
	public void touch() {
		modifications.touch();
	}

	@Override
	public boolean isDirty() {
		return modifications.isDirty();
	}

	@Override
	public Set<String> getDirtyProperties() {
		return modifications.getDirtyProperties();
	}
}
