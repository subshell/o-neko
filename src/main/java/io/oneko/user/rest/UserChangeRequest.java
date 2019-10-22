package io.oneko.user.rest;

import io.oneko.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserChangeRequest {
	private final User user;
	private final String password;
}
