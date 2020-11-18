package io.oneko.configuration;

import io.oneko.user.ONekoUserDetails;
import io.oneko.user.User;
import io.oneko.user.auth.PasswordBasedUserAuthentication;
import io.oneko.user.auth.UserAuthentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

class ONekoUserDetailsImpl implements ONekoUserDetails {

	private static final String DEFAULT_ROLE_PREFIX = "ROLE_";

	private final User user;

	public ONekoUserDetailsImpl(User user) {
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(() -> DEFAULT_ROLE_PREFIX + user.getRole().toString());
	}

	@Override
	public String getPassword() {
		UserAuthentication<?> authentication = user.getAuthentication();
		if (authentication instanceof PasswordBasedUserAuthentication) {
			return ((PasswordBasedUserAuthentication) authentication).getHashedPassword();
		}
		return null;
	}

	@Override
	public String getUsername() {
		return user.getUserName();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public User getUser() {
		return user;
	}
}
