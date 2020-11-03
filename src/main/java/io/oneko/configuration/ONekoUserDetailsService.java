package io.oneko.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import io.oneko.user.UserRepository;

@Component
public class ONekoUserDetailsService implements UserDetailsService {

	transient private final UserRepository userRepository;

	@Autowired
	public ONekoUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if (username.contains("@")) {
			return userRepository.getByUserEmail(username)
					.or(() -> this.userRepository.getByUserName(username))
					.map(ONekoUserDetailsImpl::new)
					.orElse(null);
		}

		return this.userRepository.getByUserName(username)
				.map(ONekoUserDetailsImpl::new).orElse(null);
	}
}
