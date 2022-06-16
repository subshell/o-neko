package io.oneko.helm.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.oneko.project.Project;
import io.oneko.project.ProjectVersion;

class HelmCommandUtilsTest {

	@Test
	void getLongReleaseNamePrefix() {
		var projectUuid = UUID.fromString("5524b4a7-3b43-4879-8c0d-fe1c6a95c54f");

		var project = mock(Project.class);
		when(project.getName()).thenReturn("pnamelonglong");
		when(project.getId()).thenReturn(projectUuid);

		var version = mock(ProjectVersion.class);
		when(version.getName()).thenReturn("vnamelonglong");
		when(version.getProject()).thenReturn(project);

		var prefix = HelmCommandUtils.getReleaseNamePrefix(version);
		assertThat(prefix).hasSizeLessThanOrEqualTo(29);
		assertThat(prefix).isEqualTo("pnamelongl5524b4a7-vnamelongl");
	}

	@Test
	void getShortReleaseNamePrefix() {
		var projectUuid = UUID.fromString("5524b4a7-3b43-4879-8c0d-fe1c6a95c54f");

		var project = mock(Project.class);
		when(project.getName()).thenReturn("pname");
		when(project.getId()).thenReturn(projectUuid);

		var version = mock(ProjectVersion.class);
		when(version.getName()).thenReturn("vname");
		when(version.getProject()).thenReturn(project);

		var prefix = HelmCommandUtils.getReleaseNamePrefix(version);
		assertThat(prefix).hasSizeLessThanOrEqualTo(29);
		assertThat(prefix).isEqualTo("pname5524b4a7-vname");
	}
}