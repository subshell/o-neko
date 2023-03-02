package io.oneko.security;

import io.oneko.websocket.SessionWebSocketHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Service;

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
		sessionWebSocketHandler.invalidateUserSession(session.getId());
		session.invalidate();
		response.setStatus(HttpStatus.OK.value());
	}
}
