package io.oneko.user.auth;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiTokenRepository {

	Optional<ApiToken> getByTokenHash(String tokenHash);

	List<ApiToken> getByUserId(UUID userId);

	ApiToken save(ApiToken token);

	void deleteById(UUID id);

	void deleteAllByUserId(UUID userId);
}
