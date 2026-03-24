package io.oneko.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.oneko.configuration.ONekoUserDetailsImpl;
import io.oneko.user.ReadableUser;
import io.oneko.user.UserRepository;
import io.oneko.user.auth.ApiToken;
import io.oneko.user.auth.ApiTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String TOKEN_PREFIX = "oneko_";

	private final ApiTokenRepository apiTokenRepository;
	private final UserRepository userRepository;

	public ApiTokenAuthenticationFilter(ApiTokenRepository apiTokenRepository, UserRepository userRepository) {
		this.apiTokenRepository = apiTokenRepository;
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = authHeader.substring(BEARER_PREFIX.length());

		if (!token.startsWith(TOKEN_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		String tokenHash = hashToken(token);
		ApiToken apiToken = apiTokenRepository.getByTokenHash(tokenHash).orElse(null);

		if (apiToken == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		if (apiToken.isExpired()) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		ReadableUser user = userRepository.getById(apiToken.getUserId()).orElse(null);

		if (user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		// Update last used timestamp
		apiToken.setLastUsedAt(Instant.now());
		apiTokenRepository.save(apiToken);

		ONekoUserDetailsImpl userDetails = new ONekoUserDetailsImpl(user);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities());

		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}

	public static String hashToken(String rawToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 not available", e);
		}
	}
}
