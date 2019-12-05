package io.oneko.user;

import static com.google.common.truth.Truth.*;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.oneko.security.UserRole;

public class UserTest {

	final UUID uuid = UUID.randomUUID();
	User uut;

	@Before
	public void setUp() {
		uut = User.builder()
				.email("foo@bar.com")
				.role(UserRole.ADMIN)
				.uuid(uuid)
				.build();
	}

	@Test
	public void userShouldNotBeDirtyWhenInitializedByBuilder() {
		assertThat(uut.isDirty()).isFalse();
		assertThat(uut.getDirtyProperties()).isEmpty();
	}

	@Test
	public void testUserIsDirtyAfterPasswordChange() {
		uut.setPasswordAuthentication("super-secret", new BCryptPasswordEncoder());
		assertThat(uut.getDirtyProperties()).containsExactly("authentication");
		assertThat(uut.isDirty()).isTrue();
	}
}