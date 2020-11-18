package io.oneko.security;

import io.oneko.user.UserRepository;
import io.oneko.user.rest.UserDTO;
import io.oneko.user.rest.UserDTOMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/session")
public class SessionController {

	private final UserRepository userRepository;
	private final UserDTOMapper userDTOMapper;

	public SessionController(UserRepository userRepository, UserDTOMapper userDTOMapper) {
		this.userRepository = userRepository;
		this.userDTOMapper = userDTOMapper;
	}

	@GetMapping
	public UserDTO isLoggedIn(Authentication authentication) {
		if (authentication.isAuthenticated()) {
			return userRepository.getByUserName(authentication.getName())
					.map(userDTOMapper::userToDTO)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with name " + authentication.getName() + "not found."));
		}
		throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
	}

}
