package io.oneko.event.rest;

import io.oneko.event.CurrentEventTrigger;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * This is a wrapper for all incoming authenticated requests.
 * A UserRequest for the corresponding user is then set as current event trigger.
 */
@Component
public class TrackUserRequestWebFilter implements Filter {

	private final CurrentEventTrigger currentEventTrigger;

	public TrackUserRequestWebFilter(CurrentEventTrigger currentEventTrigger) {
		this.currentEventTrigger = currentEventTrigger;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		final String userName = authentication.getName();
		try (var ignored = currentEventTrigger.forTryBlock(new UserRequest(userName))) {
			filterChain.doFilter(servletRequest, servletResponse);
		}
	}
}
