package io.oneko.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.stereotype.Service;

import io.oneko.websocket.ReactiveWebSocketHandler;
import reactor.core.publisher.Mono;

@Service
public class RestLogoutSuccessHandler implements ServerLogoutSuccessHandler {

	private final ReactiveWebSocketHandler reactiveWebSocketHandler;

	@Autowired
	public RestLogoutSuccessHandler(ReactiveWebSocketHandler reactiveWebSocketHandler) {
		this.reactiveWebSocketHandler = reactiveWebSocketHandler;
	}

	@Override
	public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
		if (authentication == null || "anonymous".equals(authentication.getPrincipal())) {
			exchange.getExchange().getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		} else {
			authentication.setAuthenticated(false);
			exchange.getExchange().getSession().subscribe(session -> {
				reactiveWebSocketHandler.invalidateSession(session.getId());
				session.invalidate();
			});
			exchange.getExchange().getResponse().setStatusCode(HttpStatus.OK);
		}


		return Mono.empty();
	}
}
