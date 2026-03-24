package io.oneko.user.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.user.auth.ApiToken;
import io.oneko.user.auth.ApiTokenRepository;

@Service
@Profile(Profiles.MONGO)
class ApiTokenMongoRepository implements ApiTokenRepository {

	private final ApiTokenMongoSpringRepository innerRepo;

	public ApiTokenMongoRepository(ApiTokenMongoSpringRepository innerRepo) {
		this.innerRepo = innerRepo;
	}

	@Override
	public Optional<ApiToken> getByTokenHash(String tokenHash) {
		return innerRepo.findByTokenHash(tokenHash).map(this::fromMongo);
	}

	@Override
	public List<ApiToken> getByUserId(UUID userId) {
		return innerRepo.findByUserId(userId).stream()
				.map(this::fromMongo)
				.collect(Collectors.toList());
	}

	@Override
	public ApiToken save(ApiToken token) {
		ApiTokenMongo saved = innerRepo.save(toMongo(token));
		return fromMongo(saved);
	}

	@Override
	public void deleteById(UUID id) {
		innerRepo.deleteById(id);
	}

	@Override
	public void deleteAllByUserId(UUID userId) {
		innerRepo.deleteAllByUserId(userId);
	}

	private ApiTokenMongo toMongo(ApiToken token) {
		return ApiTokenMongo.builder()
				.id(token.getId())
				.userId(token.getUserId())
				.name(token.getName())
				.tokenHash(token.getTokenHash())
				.createdAt(token.getCreatedAt())
				.expiresAt(token.getExpiresAt())
				.lastUsedAt(token.getLastUsedAt())
				.build();
	}

	private ApiToken fromMongo(ApiTokenMongo mongo) {
		return ApiToken.builder()
				.id(mongo.getId())
				.userId(mongo.getUserId())
				.name(mongo.getName())
				.tokenHash(mongo.getTokenHash())
				.createdAt(mongo.getCreatedAt())
				.expiresAt(mongo.getExpiresAt())
				.lastUsedAt(mongo.getLastUsedAt())
				.build();
	}
}
