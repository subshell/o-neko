package io.oneko.security;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ApiTokenAuthenticationFilterTest {

	@Test
	void testHashTokenProducesDeterministicResult() {
		String token = "oneko_abc123def456";
		String hash1 = ApiTokenAuthenticationFilter.hashToken(token);
		String hash2 = ApiTokenAuthenticationFilter.hashToken(token);

		assertThat(hash1).isEqualTo(hash2);
		assertThat(hash1).isNotEmpty();
		assertThat(hash1).hasSize(64); // SHA-256 produces 64 hex chars
	}

	@Test
	void testDifferentTokensProduceDifferentHashes() {
		String hash1 = ApiTokenAuthenticationFilter.hashToken("oneko_token1");
		String hash2 = ApiTokenAuthenticationFilter.hashToken("oneko_token2");

		assertThat(hash1).isNotEqualTo(hash2);
	}
}
