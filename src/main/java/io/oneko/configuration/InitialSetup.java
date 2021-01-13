package io.oneko.configuration;

import io.oneko.activity.ActivityPriority;
import io.oneko.event.CurrentEventTrigger;
import io.oneko.event.EventTrigger;
import io.oneko.security.UserRole;
import io.oneko.user.ReadableUser;
import io.oneko.user.UserRepository;
import io.oneko.user.WritableUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Optional;

/**
 * The initial setup is a bean that ensures that some mandatory data is given.
 */
@Slf4j
@Component
public class InitialSetup extends EventTrigger {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final CurrentEventTrigger currentEventTrigger;

	@Autowired
	public InitialSetup(UserRepository userRepository, PasswordEncoder passwordEncoder, CurrentEventTrigger currentEventTrigger) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.currentEventTrigger = currentEventTrigger;
	}

	@PostConstruct
	public void setupSystem() {
		log.info("Checking existence of user with admin rights.");
		try (var ignored = currentEventTrigger.forTryBlock(this)) {
			this.ensureAdminExists();
		}
	}

	/**
	 * Make sure that we have at least one user with admin role
	 */
	private void ensureAdminExists() {
		Optional<ReadableUser> admin = this.userRepository.getAll().stream()
				.filter(u -> UserRole.ADMIN.equals(u.getRole()))
				.findFirst();

		if (admin.isEmpty()) {
			ensureAdminUserHasAdminRoleIfExists();
			createAdmin();
		}
	}

	private void ensureAdminUserHasAdminRoleIfExists() {
		Optional<WritableUser> adminOptional = this.userRepository.getByUserName("admin").map(ReadableUser::writable);

		adminOptional.ifPresent(admin -> {
			admin.setRole(UserRole.ADMIN);
			userRepository.add(admin);
		});
	}

	private void createAdmin() {
		WritableUser newAdmin = new WritableUser();
		newAdmin.setRole(UserRole.ADMIN);
		newAdmin.setUserName("admin");
		newAdmin.setPasswordAuthentication("admin", this.passwordEncoder);
		userRepository.add(newAdmin);
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
