package io.oneko.configuration;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

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
