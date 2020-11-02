package io.oneko.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.stereotype.Service;

import io.oneko.websocket.SessionWebSocketHandler;
import reactor.core.publisher.Mono;

@Service
public class RestLogoutSuccessHandler implements ServerLogoutSuccessHandler {

	private final SessionWebSocketHandler sessionWebSocketHandler;

	@Autowired
	public RestLogoutSuccessHandler(SessionWebSocketHandler sessionWebSocketHandler) {
		this.sessionWebSocketHandler = sessionWebSocketHandler;
	}

	@Override
	public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
		if (authentication == null || "anonymous".equals(authentication.getPrincipal())) {
			exchange.getExchange().getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		} else {
			authentication.setAuthenticated(false);
			exchange.getExchange().getSession().subscribe(session -> {
				sessionWebSocketHandler.invalidateSession(session.getId());
				session.invalidate();
			});
			exchange.getExchange().getResponse().setStatusCode(HttpStatus.OK);
		}


		return Mono.empty();
	}
}
