package io.oneko.configuration;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import io.oneko.activity.ActivityPriority;
import io.oneko.event.EventTrigger;
import io.oneko.security.UserRole;
import io.oneko.user.ReadableUser;
import io.oneko.user.UserRepository;
import io.oneko.user.WritableUser;
import lombok.extern.slf4j.Slf4j;

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
