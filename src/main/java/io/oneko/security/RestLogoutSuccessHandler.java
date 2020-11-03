package io.oneko.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.stereotype.Service;

import io.oneko.websocket.SessionWebSocketHandler;

@Service
public class RestLogoutSuccessHandler implements LogoutSuccessHandler {

	private final SessionWebSocketHandler sessionWebSocketHandler;

	@Autowired
	public RestLogoutSuccessHandler(SessionWebSocketHandler sessionWebSocketHandler) {
		this.sessionWebSocketHandler = sessionWebSocketHandler;
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		if (authentication == null || "anonymous".equals(authentication.getPrincipal())) {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			return;
		}

		authentication.setAuthenticated(false);
		HttpSession session = request.getSession();
		sessionWebSocketHandler.invalidateSession(session.getId());
		session.invalidate();
		response.setStatus(HttpStatus.OK.value());
	}
}
