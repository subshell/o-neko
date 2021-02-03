package io.oneko.user.rest;

import java.util.UUID;

import io.oneko.security.UserRole;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rest API representation of a user.
 */
@Getter
@Setter
@NoArgsConstructor
@Data
public class UserDTO {
	//just used for reading access
	private UUID uuid;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private UserRole role;
	//just used when creating a new user
	private String password;
}
