package io.oneko.helm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.oneko.project.Project;
import io.oneko.project.ProjectVersion;

class HelmCommandsTest {

	private HelmCommands uut = new HelmCommands(new SimpleMeterRegistry());

	@Test
	void getLongReleaseNamePrefix() {
		var versionUuid = UUID.fromString("39b30073-df7a-46d5-b85f-4d4377baa8c0");

		var project = mock(Project.class);
		when(project.getName()).thenReturn("pnamelonglong");

		var version = mock(ProjectVersion.class);
		when(version.getName()).thenReturn("vnamelonglonglonglong");
		when(version.getId()).thenReturn(versionUuid);
		when(version.getProject()).thenReturn(project);

		var prefix = uut.getReleaseNamePrefix(version);
		assertThat(prefix).hasSizeLessThanOrEqualTo(38);
		assertThat(prefix).isEqualTo("pnamelongl-vnamelonglonglongl-39b30073");
	}

	@Test
	void getShortReleaseNamePrefix() {
		var versionUuid = UUID.fromString("39b30073-df7a-46d5-b85f-4d4377baa8c0");

		var project = mock(Project.class);
		when(project.getName()).thenReturn("pname");

		var version = mock(ProjectVersion.class);
		when(version.getName()).thenReturn("vname");
		when(version.getId()).thenReturn(versionUuid);
		when(version.getProject()).thenReturn(project);

		var prefix = uut.getReleaseNamePrefix(version);
		assertThat(prefix).hasSizeLessThanOrEqualTo(38);
		assertThat(prefix).isEqualTo("pname-vname-39b30073");
	}
}
