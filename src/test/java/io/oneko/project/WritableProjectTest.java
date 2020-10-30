package io.oneko.project;

import io.oneko.templates.WritableConfigurationTemplate;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class WritableProjectTest {

	@Test
	public void testSetDefaultConfigurationTemplates() {
		WritableProject uut = new WritableProject(UUID.randomUUID());

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
