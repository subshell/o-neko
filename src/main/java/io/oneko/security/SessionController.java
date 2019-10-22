package io.oneko.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import io.oneko.user.UserRepository;
import io.oneko.user.rest.UserDTO;
import io.oneko.user.rest.UserDTOMapper;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/session")
public class SessionController {

	private UserRepository userRepository;
	private UserDTOMapper userDTOMapper;

	public SessionController(UserRepository userRepository, UserDTOMapper userDTOMapper) {
		this.userRepository = userRepository;
		this.userDTOMapper = userDTOMapper;
	}

	@GetMapping
	public Mono<UserDTO> isLoggedIn(Authentication authentication) {
		if (authentication.isAuthenticated()) {
			return userRepository.getByUserName(authentication.getName())
					.map(userDTOMapper::userToDTO);
		}
		throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
	}

}
