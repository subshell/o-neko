package io.oneko.project;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import io.oneko.deployable.DeployableConfigurationTemplates;
import io.oneko.templates.WritableConfigurationTemplate;

class ProjectVersionTest {

	@Test
	void testCalculateConfiguration() {
		//have a bit of preparation here
		WritableProject project = new WritableProject(UUID.randomUUID());

		List<WritableConfigurationTemplate> templates = Collections.singletonList(
				WritableConfigurationTemplate.builder()
						.content(
								"This is a template with implicit variables (${PROJECT_NAME}, ${VERSION_NAME}), " +
										"default variables (${TEST1}), overwritten default variables (${TEST2}) and child variables (${TEST3})"
						)
						.id(UUID.randomUUID())
						.name("name")
						.description("desc")
						.build()
		);

		project.setDefaultConfigurationTemplates(templates);
		project.setName("o-neko");

		List<WritableTemplateVariable> defaultVariables = Arrays.asList(
				new WritableTemplateVariable(UUID.randomUUID(), "TEST1", "TEST1", Arrays.asList("aa", "cc"), true, "aa", false),
				new WritableTemplateVariable(UUID.randomUUID(), "TEST2", "TEST2", Collections.singletonList("bb"), true, "bb", false)
		);
		project.setTemplateVariables(defaultVariables);

		WritableProjectVersion version = project.createVersion("master");
		Map<String, String> versionVariables = new HashMap<>();
		versionVariables.put("TEST2", "cc");
		versionVariables.put("TEST3", "dd");
		version.setTemplateVariables(versionVariables);

		// the actual doing
		String configuration = version.calculateConfiguration();

		//check the results
		String expectedResult = "# > name (desc)\n\nThis is a template with implicit variables (o-neko, master), " +
				"default variables (aa), overwritten default variables (cc) and child variables (dd)";
		assertThat(configuration, is(expectedResult));
	}

	@Test
	void testOverwriteConfiguration() {        //have a bit of preparation here
		WritableProject project = new WritableProject(UUID.randomUUID());

		List<WritableConfigurationTemplate> templates = Arrays.asList(
				WritableConfigurationTemplate.builder()
						.content("aaa")
						.id(UUID.randomUUID())
						.name("name1")
						.build(),
				WritableConfigurationTemplate.builder()
						.content("ccc")
						.id(UUID.randomUUID())
						.name("name2")
						.build()
		);

		project.setDefaultConfigurationTemplates(templates);
		project.setName("o-neko");

		List<WritableTemplateVariable> defaultVariables = new ArrayList<>();
		project.setTemplateVariables(defaultVariables);

		WritableProjectVersion version = project.createVersion("master");

		version.setConfigurationTemplates(Collections.singletonList(
				WritableConfigurationTemplate.builder()
						.content("bbb")
						.id(UUID.randomUUID())
						.name("name1")
						.build()
		));

		final List<WritableConfigurationTemplate> calculatedConfigurationTemplates = version.getCalculatedConfigurationTemplates();
		assertThat(calculatedConfigurationTemplates.size(), is(2));
		for (WritableConfigurationTemplate template : version.getCalculatedConfigurationTemplates()) {
			assertThat(template.getName(), either(is("name1")).or(is("name2")));
			if (StringUtils.equals(template.getName(), "name1")) {
				assertThat(template.getContent(), is("bbb"));
			} else {
				assertThat(template.getContent(), is("ccc"));
			}
		}
	}

	@Test
	void testSetConfigurationTemplates() {
		WritableProject project = new WritableProject(UUID.randomUUID());
		WritableProjectVersion uut = project.createVersion("sample");

		assertThat(uut.getConfigurationTemplates(), is(empty()));

		WritableConfigurationTemplate t1 = new WritableConfigurationTemplate();
		t1.setName("test");
		uut.setConfigurationTemplates(Collections.singletonList(t1));
		assertThat(uut.getConfigurationTemplates(), hasItem(t1));

		try {
			uut.setConfigurationTemplates(Arrays.asList(t1, t1));
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(uut.getConfigurationTemplates(), hasItem(t1));
		}

		WritableConfigurationTemplate t2 = new WritableConfigurationTemplate();
		t2.setName("test");

		try {
			uut.setConfigurationTemplates(Arrays.asList(t1, t2));
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(uut.getConfigurationTemplates(), hasItem(t1));
		}
	}

	@Test
	void testDeployableConfigurationTemplates() {
		WritableProject project = new WritableProject(UUID.randomUUID());
		project.setName("project1");

		List<WritableConfigurationTemplate> templates = Collections.singletonList(
				WritableConfigurationTemplate.builder()
						.content("this template does not contain any variables")
						.id(UUID.randomUUID())
						.name("no-variables.yaml")
						.build()
		);

		project.setDefaultConfigurationTemplates(templates);

		final WritableProjectVersion version = project.createVersion("version");

		DeployableConfigurationTemplates deployableConfigurationTemplates = version.calculateDeployableConfigurationTemplates();
		assertThat(deployableConfigurationTemplates.getTemplates(), hasSize(1));

		// overwrite the template without variables
		List<WritableConfigurationTemplate> versionTemplates = Collections.singletonList(
				WritableConfigurationTemplate.builder()
						.content("this template does not contain any variables")
						.id(UUID.randomUUID())
						.name("other-template.yaml")
						.build()
		);
		version.setConfigurationTemplates(versionTemplates);
		deployableConfigurationTemplates = version.calculateDeployableConfigurationTemplates();
		assertThat(deployableConfigurationTemplates.getTemplates(), hasSize(2));
	}
}
