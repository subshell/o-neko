package io.oneko.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import io.oneko.configuration.ONekoUserDetailsService;
import io.oneko.websocket.SessionWebSocketHandler;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	private final ONekoUserDetailsService userDetailsService;
	private final SessionWebSocketHandler sessionWebSocketHandler;

	public SecurityConfiguration(ONekoUserDetailsService userDetailsService, SessionWebSocketHandler sessionWebSocketHandler) {
		this.userDetailsService = userDetailsService;
		this.sessionWebSocketHandler = sessionWebSocketHandler;
	}

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		AuthenticationEntryPoint entryPoint = ((request, response, authenticationException) -> response.setStatus(HttpStatus.UNAUTHORIZED.value()));

		http
				.csrf().disable()
				.exceptionHandling()
				.authenticationEntryPoint(entryPoint)
				.and()
				.authorizeRequests()
				.antMatchers("/ws/**").authenticated()
				.antMatchers("/api/**").authenticated()
				.antMatchers("/api/session/login").permitAll()
				.anyRequest().permitAll();

		// do not redirect on a successful login
		AuthenticationSuccessHandler noOpHandler = (request, response, authentication) -> {
		};

		http
				.userDetailsService(userDetailsService)
				.formLogin()
				.loginPage("/api/session/login")
				.successHandler(noOpHandler)
				.and()
				.logout().logoutUrl("/api/session/logout").logoutSuccessHandler(new RestLogoutSuccessHandler(sessionWebSocketHandler));

		http.httpBasic();
	}
}
