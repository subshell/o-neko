package io.oneko.event.rest;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.oneko.event.EventTrigger;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * This is a wrapper for all incoming authenticated requests.
 * A UserRequest for the corresponding user is then added to the mono context as event trigger.
 */
@Component
public class TrackUserRequestWebFilter implements WebFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return chain.filter(exchange)
				.subscriberContext(Context.of(EventTrigger.class, new UserRequest()));
	}

}
