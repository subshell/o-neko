package io.oneko.project;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import io.oneko.deployable.DeployableConfigurationTemplates;
import io.oneko.docker.DockerRegistry;
import io.oneko.templates.ConfigurationTemplate;

public class ProjectVersionTest {

	@Test
	public void testCalculateConfiguration() {
		//have a bit of preparation here
		DockerRegistry reg = new DockerRegistry();
		Project project = new Project(reg);

		List<ConfigurationTemplate> templates = Collections.singletonList(
				ConfigurationTemplate.builder()
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

		List<TemplateVariable> defaultVariables = Arrays.asList(
				new TemplateVariable(UUID.randomUUID(), "TEST1", "TEST1", Arrays.asList("aa", "cc"), true, "aa", false),
				new TemplateVariable(UUID.randomUUID(), "TEST2", "TEST2", Collections.singletonList("bb"), true, "bb", false)
		);
		project.setTemplateVariables(defaultVariables);

		ProjectVersion version = project.createVersion("master");
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
	public void testOverwriteConfiguration() {        //have a bit of preparation here
		DockerRegistry reg = new DockerRegistry();
		Project project = new Project(reg);

		List<ConfigurationTemplate> templates = Arrays.asList(
				ConfigurationTemplate.builder()
						.content("aaa")
						.id(UUID.randomUUID())
						.name("name1")
						.build(),
				ConfigurationTemplate.builder()
						.content("ccc")
						.id(UUID.randomUUID())
						.name("name2")
						.build()
		);

		project.setDefaultConfigurationTemplates(templates);
		project.setName("o-neko");

		List<TemplateVariable> defaultVariables = new ArrayList<>();
		project.setTemplateVariables(defaultVariables);

		ProjectVersion version = project.createVersion("master");

		version.setConfigurationTemplates(Collections.singletonList(
				ConfigurationTemplate.builder()
						.content("bbb")
						.id(UUID.randomUUID())
						.name("name1")
						.build()
		));

		final List<ConfigurationTemplate> calculatedConfigurationTemplates = version.getCalculatedConfigurationTemplates();
		assertThat(calculatedConfigurationTemplates.size(), is(2));
		for (ConfigurationTemplate template : version.getCalculatedConfigurationTemplates()) {
			assertThat(template.getName(), either(is("name1")).or(is("name2")));
			if (StringUtils.equals(template.getName(), "name1")) {
				assertThat(template.getContent(), is("bbb"));
			} else {
				assertThat(template.getContent(), is("ccc"));
			}
		}
	}

	@Test
	public void testSetConfigurationTemplates() {
		DockerRegistry dockerRegistry = new DockerRegistry();
		Project project = new Project(dockerRegistry);
		ProjectVersion uut = project.createVersion("sample");

		assertThat(uut.getConfigurationTemplates(), is(empty()));

		ConfigurationTemplate t1 = new ConfigurationTemplate();
		t1.setName("test");
		uut.setConfigurationTemplates(Collections.singletonList(t1));
		assertThat(uut.getConfigurationTemplates(), hasItem(t1));

		try {
			uut.setConfigurationTemplates(Arrays.asList(t1, t1));
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(uut.getConfigurationTemplates(), hasItem(t1));
		}

		ConfigurationTemplate t2 = new ConfigurationTemplate();
		t2.setName("test");

		try {
			uut.setConfigurationTemplates(Arrays.asList(t1, t2));
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(uut.getConfigurationTemplates(), hasItem(t1));
		}
	}

	@Test
	public void testDeployableConfigurationTemplates() {
		//have a bit of preparation here
		DockerRegistry reg = new DockerRegistry();
		Project project = new Project(reg);
		project.setName("project1");

		List<ConfigurationTemplate> templates = Collections.singletonList(
				ConfigurationTemplate.builder()
						.content("this template does not contain any variables")
						.id(UUID.randomUUID())
						.name("no-variables.yaml")
						.build()
		);

		project.setDefaultConfigurationTemplates(templates);

		final ProjectVersion version = project.createVersion("version");

		DeployableConfigurationTemplates deployableConfigurationTemplates = version.calculateDeployableConfigurationTemplates();
		assertThat(deployableConfigurationTemplates.getTemplates(), hasSize(1));

		// overwrite the template without variables
		List<ConfigurationTemplate> versionTemplates = Collections.singletonList(
				ConfigurationTemplate.builder()
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
