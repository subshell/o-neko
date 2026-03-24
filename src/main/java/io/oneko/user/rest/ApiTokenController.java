package io.oneko.user.rest;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.oneko.configuration.Controllers;
import io.oneko.security.ApiTokenAuthenticationFilter;
import io.oneko.user.ReadableUser;
import io.oneko.user.UserRepository;
import io.oneko.user.auth.ApiToken;
import io.oneko.user.auth.ApiTokenRepository;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(ApiTokenController.PATH)
public class ApiTokenController {

	public static final String PATH = Controllers.ROOT_PATH + "/user/{userName}/tokens";

	private static final String TOKEN_PREFIX = "oneko_";
	private static final int TOKEN_RANDOM_BYTES = 20;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final ApiTokenRepository apiTokenRepository;
	private final UserRepository userRepository;

	public ApiTokenController(ApiTokenRepository apiTokenRepository, UserRepository userRepository) {
		this.apiTokenRepository = apiTokenRepository;
		this.userRepository = userRepository;
	}

	@PreAuthorize("hasRole('ADMIN') OR #userName == authentication.name")
	@GetMapping
	List<ApiTokenDTO> getTokens(@PathVariable String userName) {
		ReadableUser user = getUserOrThrow(userName);
		return apiTokenRepository.getByUserId(user.getId()).stream()
				.map(ApiTokenDTO::fromApiToken)
				.collect(Collectors.toList());
	}

	@PreAuthorize("hasRole('ADMIN') OR #userName == authentication.name")
	@PostMapping
	ApiTokenDTO.CreateApiTokenResponse createToken(@PathVariable String userName, @RequestBody ApiTokenDTO.CreateApiTokenRequest request) {
		ReadableUser user = getUserOrThrow(userName);

		if (request.getExpiresAt() != null) {
			Instant maxExpiry = Instant.now().plus(365, java.time.temporal.ChronoUnit.DAYS);
			if (request.getExpiresAt().isAfter(maxExpiry)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expiry cannot be more than 1 year from now.");
			}
		}

		String rawToken = generateRawToken();
		String tokenHash = ApiTokenAuthenticationFilter.hashToken(rawToken);

		ApiToken apiToken = ApiToken.builder()
				.id(UUID.randomUUID())
				.userId(user.getId())
				.name(request.getName())
				.tokenHash(tokenHash)
				.createdAt(Instant.now())
				.expiresAt(request.getExpiresAt())
				.build();

		ApiToken saved = apiTokenRepository.save(apiToken);

		log.info("API token '{}' created for user '{}'", request.getName(), userName);

		return new ApiTokenDTO.CreateApiTokenResponse(ApiTokenDTO.fromApiToken(saved), rawToken);
	}

	@PreAuthorize("hasRole('ADMIN') OR #userName == authentication.name")
	@DeleteMapping("/{tokenId}")
	void deleteToken(@PathVariable String userName, @PathVariable UUID tokenId) {
		getUserOrThrow(userName);
		apiTokenRepository.deleteById(tokenId);
		log.info("API token '{}' deleted for user '{}'", tokenId, userName);
	}

	private ReadableUser getUserOrThrow(String userName) {
		return userRepository.getByUserName(userName)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with name " + userName + " not found."));
	}

	private String generateRawToken() {
		byte[] randomBytes = new byte[TOKEN_RANDOM_BYTES];
		SECURE_RANDOM.nextBytes(randomBytes);
		return TOKEN_PREFIX + HexFormat.of().formatHex(randomBytes);
	}
}
