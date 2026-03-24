package io.oneko.user.persistence;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.oneko.user.auth.ApiToken;

class ApiTokenInMemoryRepositoryTest {

	private ApiTokenInMemoryRepository uut;

	@BeforeEach
	void setup() {
		uut = new ApiTokenInMemoryRepository();
	}

	@Test
	void testCrud() {
		UUID userId = UUID.randomUUID();
		ApiToken token = ApiToken.builder()
				.id(UUID.randomUUID())
				.userId(userId)
				.name("test-token")
				.tokenHash("abc123hash")
				.createdAt(Instant.now())
				.build();

		uut.save(token);
		assertThat(uut.getByUserId(userId)).hasSize(1);

		uut.deleteById(token.getId());
		assertThat(uut.getByUserId(userId)).isEmpty();
	}

	@Test
	void testGetByTokenHash() {
		ApiToken token = ApiToken.builder()
				.id(UUID.randomUUID())
				.userId(UUID.randomUUID())
				.name("my-token")
				.tokenHash("unique-hash-value")
				.createdAt(Instant.now())
				.build();

		uut.save(token);
		assertThat(uut.getByTokenHash("unique-hash-value")).isPresent();
		assertThat(uut.getByTokenHash("nonexistent")).isEmpty();
	}

	@Test
	void testDeleteAllByUserId() {
		UUID userId = UUID.randomUUID();

		for (int i = 0; i < 3; i++) {
			uut.save(ApiToken.builder()
					.id(UUID.randomUUID())
					.userId(userId)
					.name("token-" + i)
					.tokenHash("hash-" + i)
					.createdAt(Instant.now())
					.build());
		}

		// Also save a token for a different user
		UUID otherUserId = UUID.randomUUID();
		uut.save(ApiToken.builder()
				.id(UUID.randomUUID())
				.userId(otherUserId)
				.name("other-token")
				.tokenHash("other-hash")
				.createdAt(Instant.now())
				.build());

		assertThat(uut.getByUserId(userId)).hasSize(3);
		assertThat(uut.getByUserId(otherUserId)).hasSize(1);

		uut.deleteAllByUserId(userId);

		assertThat(uut.getByUserId(userId)).isEmpty();
		assertThat(uut.getByUserId(otherUserId)).hasSize(1);
	}

	@Test
	void testIsExpired() {
		ApiToken expiredToken = ApiToken.builder()
				.id(UUID.randomUUID())
				.userId(UUID.randomUUID())
				.name("expired")
				.tokenHash("expired-hash")
				.createdAt(Instant.now().minus(2, ChronoUnit.DAYS))
				.expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
				.build();

		ApiToken validToken = ApiToken.builder()
				.id(UUID.randomUUID())
				.userId(UUID.randomUUID())
				.name("valid")
				.tokenHash("valid-hash")
				.createdAt(Instant.now())
				.expiresAt(Instant.now().plus(30, ChronoUnit.DAYS))
				.build();

		ApiToken noExpiryToken = ApiToken.builder()
				.id(UUID.randomUUID())
				.userId(UUID.randomUUID())
				.name("no-expiry")
				.tokenHash("no-expiry-hash")
				.createdAt(Instant.now())
				.build();

		assertThat(expiredToken.isExpired()).isTrue();
		assertThat(validToken.isExpired()).isFalse();
		assertThat(noExpiryToken.isExpired()).isFalse();
	}
}
