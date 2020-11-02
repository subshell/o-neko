package io.oneko.user.rest;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.oneko.configuration.Controllers;
import io.oneko.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@RequestMapping(AvailableController.PATH)
public class AvailableController {

	public static final String PATH = Controllers.ROOT_PATH + "/available";

	private final UserRepository userRepository;

	public AvailableController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/username/{userName}")
	AvailableDTO isUsernameAvailable(@PathVariable String userName) {
		return userRepository.getByUserName(userName)
				.map(exists -> new AvailableDTO(userName, false))
				.orElseThrow(()  -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with name " + userName + "not found"));
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/email/{email}")
	AvailableDTO isEmailAvailable(@PathVariable String email) {
		return userRepository.getByUserEmail(email)
				.map(exists -> new AvailableDTO(email, false))
				.orElseThrow(()  -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with email " + email + "not found"));
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class AvailableDTO {
		private String value;
		private boolean available;
	}

}
