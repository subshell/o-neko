package io.oneko.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;

import io.oneko.configuration.UserDetailsService;
import io.oneko.websocket.SessionWebSocketHandler;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final SessionWebSocketHandler sessionWebSocketHandler;

	public SecurityConfiguration(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder, SessionWebSocketHandler sessionWebSocketHandler) {
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
		this.sessionWebSocketHandler = sessionWebSocketHandler;
	}

	@Bean
	SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
		UserDetailsRepositoryReactiveAuthenticationManager userDetailsManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
		userDetailsManager.setPasswordEncoder(passwordEncoder);

		ServerAuthenticationEntryPoint entryPoint = ((exchange, e) -> {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return Mono.empty();
		});

		return http
				.csrf().disable()
				.exceptionHandling()
				.authenticationEntryPoint(entryPoint)
				.and()
				.authorizeExchange()
				.pathMatchers("/ws/**").authenticated()
				.pathMatchers("/api/**").authenticated()
				.pathMatchers("/api/session/login").permitAll()
				.anyExchange().permitAll()
				.and()
				.formLogin()
				.loginPage("/api/session/login")
				.authenticationManager(userDetailsManager)
				.authenticationSuccessHandler(new NoopServerAuthenticationSuccessHandler())
				.authenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(entryPoint))
				.and()
				.logout().logoutUrl("/api/session/logout").logoutSuccessHandler(new RestLogoutSuccessHandler(sessionWebSocketHandler))
				.and()
				.httpBasic()
				.and()
				.build();
	}

}
