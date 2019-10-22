package io.oneko.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.oneko.user.UserRepository;
import reactor.core.publisher.Mono;

@Component
public class UserDetailsService implements ReactiveUserDetailsService {

	transient private final UserRepository userRepository;

	@Autowired
	public UserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public Mono<UserDetails> findByUsername(String username) {
		if (username.contains("@")) {
			return userRepository.getByUserEmail(username)
					.switchIfEmpty(this.userRepository.getByUserName(username))
					.map(ONekoUserDetailsImpl::new);
		}
		return this.userRepository.getByUserName(username)
				.map(ONekoUserDetailsImpl::new);
	}

}
