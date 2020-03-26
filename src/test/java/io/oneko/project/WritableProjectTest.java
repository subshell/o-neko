package io.oneko.project;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import io.oneko.docker.ReadableDockerRegistry;
import io.oneko.docker.WritableDockerRegistry;
import org.junit.Test;

import io.oneko.docker.DockerRegistry;
import io.oneko.templates.WritableConfigurationTemplate;

public class WritableProjectTest {

	@Test
	public void testSetDefaultConfigurationTemplates() {
		ReadableDockerRegistry dockerRegistry = new WritableDockerRegistry().readable();
		WritableProject uut = new WritableProject(dockerRegistry);

		assertThat(uut.getDefaultConfigurationTemplates(), is(empty()));

		WritableConfigurationTemplate t1 = new WritableConfigurationTemplate();
		t1.setName("test");
		uut.setDefaultConfigurationTemplates(Collections.singletonList(t1));
		assertThat(uut.getDefaultConfigurationTemplates(), hasItem(t1));

		try {
			uut.setDefaultConfigurationTemplates(Arrays.asList(t1, t1));
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(uut.getDefaultConfigurationTemplates(), hasItem(t1));
		}

		WritableConfigurationTemplate t2 = new WritableConfigurationTemplate();
		t2.setName("test");

		try {
			uut.setDefaultConfigurationTemplates(Arrays.asList(t1, t2));
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(uut.getDefaultConfigurationTemplates(), hasItem(t1));
		}
	}

}
