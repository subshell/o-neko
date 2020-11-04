package io.oneko.project;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.oneko.automations.LifetimeBehaviour;
import io.oneko.templates.WritableConfigurationTemplate;

public class WritableProjectTest {

	@Test
	void testReadable() {
		//prepare a writable
		UUID dockerRegistryId = UUID.randomUUID();
		WritableProject uut = new WritableProject(dockerRegistryId);
		uut.setName("test");
		uut.setImageName("subshell/test");
		uut.setDefaultLifetimeBehaviour(LifetimeBehaviour.infinite());
		WritableProjectVersion version1 = uut.createVersion("12");
		version1.setDockerContentDigest("digest1");

		WritableProjectVersion version2 = uut.createVersion("42");
		version2.setDockerContentDigest("digest2");

		//create a readable and check it's properties
		ReadableProject readable = uut.readable();

		assertThat(readable.getId()).isEqualTo(uut.getId());
		assertThat(readable.getName()).isEqualTo(uut.getName());
		assertThat(readable.getImageName()).isEqualTo(uut.getImageName());
		assertThat(readable.getDockerRegistryId()).isEqualTo(uut.getDockerRegistryId());

		assertThat(readable.getVersions()).hasSize(2);

		ReadableProjectVersion rVersion1 = readable.getVersions().get(0);
		assertThat(rVersion1.getId()).isEqualTo(version1.getId());
		assertThat(rVersion1.getName()).isEqualTo(version1.getName());
		assertThat(rVersion1.getDockerContentDigest()).isEqualTo(version1.getDockerContentDigest());

		ReadableProjectVersion rVersion2 = readable.getVersions().get(1);
		assertThat(rVersion2.getId()).isEqualTo(version2.getId());
		assertThat(rVersion2.getName()).isEqualTo(version2.getName());
		assertThat(rVersion2.getDockerContentDigest()).isEqualTo(version2.getDockerContentDigest());

		//modifications to writable must not be propagated to readable

		uut.setName("New Test Name");
		uut.removeVersion(version1.getName());

		assertThat(readable.getName()).isEqualTo("test");
		assertThat(readable.getVersions()).hasSize(2);
	}

	@Test
	void testSetDefaultConfigurationTemplates() {
		WritableProject uut = new WritableProject(UUID.randomUUID());

		assertThat(uut.getDefaultConfigurationTemplates()).isEmpty();

		WritableConfigurationTemplate t1 = new WritableConfigurationTemplate();
		t1.setName("test");
		uut.setDefaultConfigurationTemplates(Collections.singletonList(t1));
		assertThat(uut.getDefaultConfigurationTemplates()).containsExactly(t1);

		assertThatThrownBy(() -> uut.setDefaultConfigurationTemplates(Arrays.asList(t1, t1))).isInstanceOf(IllegalArgumentException.class);
		assertThat(uut.getDefaultConfigurationTemplates()).containsExactly(t1);

		WritableConfigurationTemplate t2 = new WritableConfigurationTemplate();
		t2.setName("test");

		assertThatThrownBy(() -> uut.setDefaultConfigurationTemplates(Arrays.asList(t1, t1))).isInstanceOf(IllegalArgumentException.class);
		assertThat(uut.getDefaultConfigurationTemplates()).containsExactly(t1);
	}

}
