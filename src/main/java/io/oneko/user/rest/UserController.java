package io.oneko.user.rest;

import io.oneko.user.ReadableUser;
import io.oneko.user.WritableUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.oneko.configuration.Controllers;
import io.oneko.security.UserRole;
import io.oneko.user.ONekoUserDetails;
import io.oneko.user.User;
import io.oneko.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(UserController.PATH)
public class UserController {

	public static final String PATH = Controllers.ROOT_PATH + "/user";

	private final UserRepository userRepository;
	private final UserDTOMapper dtoMapper;
	private final PasswordEncoder passwordEncoder;

	public UserController(UserRepository userRepository, UserDTOMapper dtoMapper, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.dtoMapper = dtoMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping
	Flux<UserDTO> getAllUsers() {
		return this.userRepository.getAll().map(this.dtoMapper::userToDTO);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	Mono<UserDTO> createUser(@RequestBody UserDTO dto) {
		WritableUser newUser = new WritableUser();
		newUser.setPasswordAuthentication(dto.getPassword(), this.passwordEncoder);
		this.dtoMapper.updateUserFromDTO(newUser, dto);
		return userRepository.add(newUser).map(this.dtoMapper::userToDTO);
	}

	@PreAuthorize("hasRole('ADMIN') OR #userName == authentication.name")
	@GetMapping("/{userName}")
	Mono<UserDTO> getUserById(@PathVariable String userName) {
		return this.userRepository.getByUserName(userName).map(this.dtoMapper::userToDTO);
	}

	@PreAuthorize("hasRole('ADMIN') OR #userName == authentication.name")
	@PostMapping("/{userName}")
	Mono<UserDTO> updateUser(Authentication authentication, @AuthenticationPrincipal Mono<ONekoUserDetails> userDetails, @PathVariable String userName, @RequestBody UserDTO dto) {
		Mono<User> updateUserMono = this.userRepository.getByUserName(userName)
				.map(ReadableUser::writable)
				.map(u -> this.dtoMapper.updateUserFromDTO(u, dto))
				.flatMap(this.userRepository::add);

		return userDetails
				.map(ONekoUserDetails::getUser)
				.filter(user -> user.getRole().equals(UserRole.ADMIN) || user.getRole().equals(dto.getRole()))
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot change your own role! ...unless you are ADMIN, because then you are almighty.")))
				.then(updateUserMono)
				.doOnNext(user -> {
					if (!userName.equals(dto.getUsername()) && ((ONekoUserDetails) authentication.getPrincipal()).getUser().getUserName().equals(userName)) { // when username is changed by that user, the authentication (which is bound to the old username) needs to be revoked
						authentication.setAuthenticated(false);
					}
				}).map(this.dtoMapper::userToDTO);
	}

	@PreAuthorize("hasRole('ADMIN') OR #userName == authentication.name")
	@PostMapping("/{userName}/password")
	Mono<UserDTO> changePassword(@PathVariable String userName, @RequestBody ChangePasswordDTO dto) {
		return this.userRepository.getByUserName(userName)
				.map(ReadableUser::writable)
				.doOnNext(u -> u.setPasswordAuthentication(dto.getPassword(), this.passwordEncoder))
				.flatMap(this.userRepository::add)
				.map(this.dtoMapper::userToDTO);
	}

	@PreAuthorize("hasRole('ADMIN') OR #userName == authentication.name")
	@DeleteMapping("/{userName}")
	Mono<Void> deleteUser(@PathVariable String userName) {
		return this.userRepository.getByUserName(userName).flatMap(this.userRepository::removeUser);
	}

}
