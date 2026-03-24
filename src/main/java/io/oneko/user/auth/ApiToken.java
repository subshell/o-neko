package io.oneko.user.auth;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ApiToken {

	private UUID id;
	private UUID userId;
	private String name;
	private String tokenHash;
	private Instant createdAt;
	private Instant expiresAt;
	private Instant lastUsedAt;

	public boolean isExpired() {
		return expiresAt != null && Instant.now().isAfter(expiresAt);
	}
}
