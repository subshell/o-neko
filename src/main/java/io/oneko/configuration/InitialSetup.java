package io.oneko.configuration;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import io.oneko.activity.ActivityPriority;
import io.oneko.event.EventTrigger;
import io.oneko.security.UserRole;
import io.oneko.user.User;
import io.oneko.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * The initial setup is a bean that ensures that some mandatory data is given.
 */
@Slf4j
@Component
public class InitialSetup extends EventTrigger {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public InitialSetup(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@PostConstruct
	public void setupSystem() {
		log.info("Checking existence of user with admin rights.");
		this.ensureAdminExists();
	}

	/**
	 * Make sure that we have at least one user with admin role
	 */
	private void ensureAdminExists() {
		this.userRepository.getAll()
				.filter(u -> UserRole.ADMIN.equals(u.getRole()))
				.switchIfEmpty(ensureAdminUserHasAdminRoleIfExists())
				.switchIfEmpty(createAdmin())
				.subscriberContext(Context.of(EventTrigger.class, this))
				.subscribe(null, e -> log.error(e.getMessage(), e));
	}

	private Mono<User> ensureAdminUserHasAdminRoleIfExists() {
		return this.userRepository.getByUserName("admin")
				.doOnNext(user -> user.setRole(UserRole.ADMIN))
				.flatMap(userRepository::add);
	}

	private Mono<User> createAdmin() {
		User newAdmin = new User();
		newAdmin.setRole(UserRole.ADMIN);
		newAdmin.setUserName("admin");
		newAdmin.setPasswordAuthentication("admin", this.passwordEncoder);
		return userRepository.add(newAdmin);
	}

	@Override
	public String humanReadable() {
		return "Initial system setup";
	}

	@Override
	public ActivityPriority priority() {
		return ActivityPriority.INFO;
	}

	@Override
	public String getType() {
		return "setup";
	}
}
