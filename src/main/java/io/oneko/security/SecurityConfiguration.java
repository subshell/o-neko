package io.oneko.security;

import io.oneko.configuration.ONekoUserDetailsService;
import io.oneko.websocket.SessionWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

	private final ONekoUserDetailsService userDetailsService;
	private final SessionWebSocketHandler sessionWebSocketHandler;
	private final ApiTokenAuthenticationFilter apiTokenAuthenticationFilter;

	public SecurityConfiguration(ONekoUserDetailsService userDetailsService, SessionWebSocketHandler sessionWebSocketHandler, ApiTokenAuthenticationFilter apiTokenAuthenticationFilter) {
		this.userDetailsService = userDetailsService;
		this.sessionWebSocketHandler = sessionWebSocketHandler;
		this.apiTokenAuthenticationFilter = apiTokenAuthenticationFilter;
	}

	@Bean
	protected SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
		AuthenticationEntryPoint entryPoint = ((request, response, authenticationException) -> response.setStatus(HttpStatus.UNAUTHORIZED.value()));

		http
				.csrf().disable()
				.exceptionHandling()
				.authenticationEntryPoint(entryPoint)
				.and()
				.authorizeHttpRequests((authz) -> authz
						.requestMatchers("/ws/**").authenticated()
						.requestMatchers("/api/**").authenticated()
						.requestMatchers("/api/session/login").permitAll()
						.anyRequest().permitAll()
				);

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

		http.addFilterBefore(apiTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.httpBasic().and()
				.build();
	}
}
