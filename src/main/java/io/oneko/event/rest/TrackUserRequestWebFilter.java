package io.oneko.event.rest;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.oneko.event.CurrentEventTrigger;

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
