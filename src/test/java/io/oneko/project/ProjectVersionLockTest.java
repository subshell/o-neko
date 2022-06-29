package io.oneko.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.UUID;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

class ProjectVersionLockTest {

	@Test
	void currentThreadHasLock() {
		ProjectVersionLock uut = new ProjectVersionLock();

		UUID uuid = UUID.randomUUID();
		ProjectVersion<?, ?> projectVersion = mock(ProjectVersion.class);
		when(projectVersion.getId()).thenReturn(uuid);

		assertThat(uut.currentThreadHasLock(uuid)).isFalse();

		uut.doWithProjectVersionLock(projectVersion, () -> {
			assertThat(uut.currentThreadHasLock(uuid)).isTrue();
		});

		assertThat(uut.currentThreadHasLock(uuid)).isFalse();

		ProjectVersion<?, ?> otherProjectVersion = mock(ProjectVersion.class);
		when(otherProjectVersion.getId()).thenReturn(UUID.randomUUID());
		uut.doWithProjectVersionLock(otherProjectVersion, () -> {
			assertThat(uut.currentThreadHasLock(uuid)).isFalse();
		});
	}

	@Test
	void threadOwnsLock() {
		ProjectVersionLock uut = new ProjectVersionLock();
		UUID uuid = UUID.randomUUID();
		ProjectVersion<?, ?> projectVersion = mock(ProjectVersion.class);
		when(projectVersion.getId()).thenReturn(uuid);

		Awaitility.waitAtMost(Duration.ofSeconds(10))
				.until(() -> {
					return uut.doWithProjectVersionLock(projectVersion, () -> {
						return uut.doWithProjectVersionLock(projectVersion, () -> uut.currentThreadHasLock(uuid));
					});
				});

		verify(projectVersion, atLeastOnce()).getId();
	}

}