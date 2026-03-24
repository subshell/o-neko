package io.oneko.user.rest;

import java.time.Instant;
import java.util.UUID;

import io.oneko.user.auth.ApiToken;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiTokenDTO {

	private UUID id;
	private String name;
	private Instant createdAt;
	private Instant expiresAt;
	private Instant lastUsedAt;

	public static ApiTokenDTO fromApiToken(ApiToken token) {
		ApiTokenDTO dto = new ApiTokenDTO();
		dto.setId(token.getId());
		dto.setName(token.getName());
		dto.setCreatedAt(token.getCreatedAt());
		dto.setExpiresAt(token.getExpiresAt());
		dto.setLastUsedAt(token.getLastUsedAt());
		return dto;
	}

	/**
	 * Response for token creation. Contains the raw token which is only shown once.
	 */
	@Data
	@NoArgsConstructor
	public static class CreateApiTokenRequest {
		private String name;
		private Instant expiresAt;
	}

	@Data
	public static class CreateApiTokenResponse {
		private final ApiTokenDTO token;
		private final String rawToken;
	}
}
