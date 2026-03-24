package io.oneko.user.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.user.auth.ApiToken;
import io.oneko.user.auth.ApiTokenRepository;

@Service
@Profile(Profiles.IN_MEMORY)
public class ApiTokenInMemoryRepository implements ApiTokenRepository {

	private final Map<UUID, ApiToken> tokens = new HashMap<>();

	@Override
	public Optional<ApiToken> getByTokenHash(String tokenHash) {
		return tokens.values().stream()
				.filter(t -> t.getTokenHash().equals(tokenHash))
				.findFirst();
	}

	@Override
	public List<ApiToken> getByUserId(UUID userId) {
		return tokens.values().stream()
				.filter(t -> t.getUserId().equals(userId))
				.collect(Collectors.toList());
	}

	@Override
	public ApiToken save(ApiToken token) {
		tokens.put(token.getId(), token);
		return token;
	}

	@Override
	public void deleteById(UUID id) {
		tokens.remove(id);
	}

	@Override
	public void deleteAllByUserId(UUID userId) {
		tokens.entrySet().removeIf(e -> e.getValue().getUserId().equals(userId));
	}
}
