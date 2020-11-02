package io.oneko.user.rest;

import io.oneko.user.*;
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
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

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
	List<UserDTO> getAllUsers() {
		return this.userRepository.getAll().stream().map(this.dtoMapper::userToDTO).collect(Collectors.toList());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	UserDTO createUser(@RequestBody UserDTO dto) {
		WritableUser newUser = new WritableUser();
		newUser.setPasswordAuthentication(dto.getPassword(), this.passwordEncoder);
		this.dtoMapper.updateUserFromDTO(newUser, dto);
		ReadableUser persistedUser = userRepository.add(newUser);
		return this.dtoMapper.userToDTO(persistedUser);
	}

	@PreAuthorize("hasRole('ADMIN') OR #userName == authentication.name")
	@GetMapping("/{userName}")
	UserDTO getUserById(@PathVariable String userName) {
		return this.userRepository.getByUserName(userName)
				.map(this.dtoMapper::userToDTO)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with name " + userName + "not found."));
	}

	@PreAuthorize("hasRole('ADMIN') OR #userName == authentication.name")
	@PostMapping("/{userName}")
	UserDTO updateUser(Authentication authentication, @AuthenticationPrincipal ONekoUserDetails userDetails, @PathVariable String userName, @RequestBody UserDTO dto) {
		User accessingUser = userDetails.getUser();
		if (accessingUser.getRole() != UserRole.ADMIN && accessingUser.getRole() != dto.getRole()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to change your own role!");
		}

		WritableUser user = this.userRepository.getByUserName(userName)
				.orElseThrow(() -> new RuntimeException("User with name " + userName + "not found.")).writable();
		this.dtoMapper.updateUserFromDTO(user, dto);
		ReadableUser persistedUser = this.userRepository.add(user);

		if (!userName.equals(dto.getUsername()) && ((ONekoUserDetails) authentication.getPrincipal()).getUser().getUserName().equals(userName)) { // when username is changed by that user, the authentication (which is bound to the old username) needs to be revoked
			authentication.setAuthenticated(false);
		}

		return dtoMapper.userToDTO(persistedUser);
	}

	@PreAuthorize("hasRole('ADMIN') OR #userName == authentication.name")
	@PostMapping("/{userName}/password")
	UserDTO changePassword(@PathVariable String userName, @RequestBody ChangePasswordDTO dto) {
		ReadableUser user = this.userRepository.getByUserName(userName)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with name " + userName + "not found."));
		WritableUser writableUser = user.writable();
		writableUser.setPasswordAuthentication(dto.getPassword(), this.passwordEncoder);
		ReadableUser persistedUser = this.userRepository.add(writableUser);
		return this.dtoMapper.userToDTO(persistedUser);
	}

	@PreAuthorize("hasRole('ADMIN') OR #userName == authentication.name")
	@DeleteMapping("/{userName}")
	void deleteUser(@PathVariable String userName) {
		ReadableUser user = this.userRepository.getByUserName(userName)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with name " + userName + "not found."));
		this.userRepository.removeUser(user);
	}

}
