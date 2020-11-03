package io.oneko.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

import io.oneko.configuration.ONekoUserDetailsService;
import io.oneko.websocket.SessionWebSocketHandler;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	private final ONekoUserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final SessionWebSocketHandler sessionWebSocketHandler;

	public SecurityConfiguration(ONekoUserDetailsService userDetailsService, PasswordEncoder passwordEncoder, SessionWebSocketHandler sessionWebSocketHandler) {
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
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

		http
				.userDetailsService(userDetailsService)
				.formLogin()
				.loginPage("/api/session/login")
				.and()
				.logout().logoutUrl("/api/session/logout").logoutSuccessHandler(new RestLogoutSuccessHandler(sessionWebSocketHandler));

		// TODO new AuthenticationEntryPointFailureHandler(entryPoint);

		http.httpBasic();
	}
}
