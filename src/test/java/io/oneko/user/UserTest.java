package io.oneko.user;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.oneko.security.UserRole;

class UserTest {

	final UUID uuid = UUID.randomUUID();
	WritableUser uut;

	@BeforeEach
	void setUp() {
		uut = WritableUser.builder()
				.email("foo@bar.com")
				.role(UserRole.ADMIN)
				.uuid(uuid)
				.build();
	}

	@Test
	void userShouldNotBeDirtyWhenInitializedByBuilder() {
		assertThat(uut.isDirty()).isFalse();
		assertThat(uut.getDirtyProperties()).isEmpty();
	}

	@Test
	void testUserIsDirtyAfterPasswordChange() {
		uut.setPasswordAuthentication("super-secret", new BCryptPasswordEncoder());
		assertThat(uut.getDirtyProperties()).containsExactly("authentication");
		assertThat(uut.isDirty()).isTrue();
	}
}
