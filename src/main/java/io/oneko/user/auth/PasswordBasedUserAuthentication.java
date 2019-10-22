package io.oneko.user.auth;

import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.security.crypto.password.PasswordEncoder;

import io.oneko.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * Straight-forward implementation of user authentication using string based passwords.
 * <br/>
 * As default as possible
 */
@Data
@Builder
@AllArgsConstructor
public class PasswordBasedUserAuthentication implements UserAuthentication<String> {

	@Getter
	private UUID userId;
	private Supplier<User> userSupplier;
	private String hashedPassword;
	private PasswordEncoder passwordEncoder;


	@Override
	public User getUser() {
		return userSupplier.get();
	}

	@Override
	public Class<String> getIdentifierType() {
		return String.class;
	}

	@Override
	public boolean authenticates(String rawPassword) {
		return this.passwordEncoder.matches(rawPassword, this.hashedPassword);
	}

}
