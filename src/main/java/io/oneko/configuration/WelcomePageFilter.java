package io.oneko.configuration;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class WelcomePageFilter implements Filter {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			final var requestURI = ((HttpServletRequest) request).getRequestURI();
			if (StringUtils.equals(requestURI, "/")) {
				request.getRequestDispatcher("/index.html").forward(request, response);
				return;
			}
		}

		chain.doFilter(request, response);
	}
}
