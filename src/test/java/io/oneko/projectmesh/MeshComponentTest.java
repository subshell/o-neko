package io.oneko.projectmesh;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import io.oneko.docker.DockerRegistry;
import io.oneko.project.Project;
import io.oneko.project.ProjectVersion;
import io.oneko.project.TemplateVariable;
import io.oneko.templates.ConfigurationTemplate;

public class MeshComponentTest {

	@Test
	public void testChangeVersion() {
		DockerRegistry reg = new DockerRegistry();
		Project p = new Project(reg);
		final ProjectVersion v1 = p.createVersion("v1");
		final ProjectVersion v2 = p.createVersion("v2");

		Project someOtherProject = new Project(reg);
		final ProjectVersion someOtherProjectsVersion = someOtherProject.createVersion("v2");

		ProjectMesh mesh = new ProjectMesh();
		MeshComponent c = new MeshComponent(mesh, p, v1);

		assertThat(c.getProjectVersion(), is(v1));

		c.setProjectVersion(v2);
		assertThat(c.getProjectVersion(), is(v2));

		try {
			c.setProjectVersion(someOtherProjectsVersion);
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(c.getProjectVersion(), is(v2));
		}
	}

	@Test
	public void testTemplateComposition() {
		DockerRegistry r = new DockerRegistry();
		Project p = new Project(r);
		p.setDefaultConfigurationTemplates(Arrays.asList(
				new ConfigurationTemplate(UUID.randomUUID(), "content1 ${a}", "name1", ""),
				new ConfigurationTemplate(UUID.randomUUID(), "content2 ${b}", "name2", ""),
				new ConfigurationTemplate(UUID.randomUUID(), "content3 ${c}", "name3", ""),
				new ConfigurationTemplate(UUID.randomUUID(), "content4 ${d}", "name4", "")));
		List<TemplateVariable> defaultTemplateVariables = new ArrayList<>();
		defaultTemplateVariables.add(new TemplateVariable("a", "a", Collections.singletonList("a1"), true, "a1", false));
		defaultTemplateVariables.add(new TemplateVariable("b", "b", Collections.singletonList("b1"), true, "b1", false));
		defaultTemplateVariables.add(new TemplateVariable("c", "b", Collections.singletonList("c1"), true, "c1", false));
		defaultTemplateVariables.add(new TemplateVariable("d", "b", Collections.singletonList("d1"), true, "d1", false));
		p.setTemplateVariables(defaultTemplateVariables);

		final ProjectVersion v = p.createVersion("v1");
		v.setConfigurationTemplates(Arrays.asList(
				new ConfigurationTemplate(UUID.randomUUID(), "content3 ${c} from version", "name3", ""),
				new ConfigurationTemplate(UUID.randomUUID(), "content4 ${d} from version", "name4", ""),
				new ConfigurationTemplate(UUID.randomUUID(), "content5 ${e} from version", "name5", "")));

		Map<String, String> templateVariables = new HashMap<>();
		templateVariables.put("b", "b2");
		templateVariables.put("e", "e2");
		v.setTemplateVariables(templateVariables);

		ProjectMesh mesh = new ProjectMesh();
		MeshComponent c = new MeshComponent(mesh, p, v);
		c.setConfigurationTemplates(Arrays.asList(
				new ConfigurationTemplate(UUID.randomUUID(), "content2 ${b} from mesh", "name2", ""),
				new ConfigurationTemplate(UUID.randomUUID(), "content4 ${d} from mesh", "name4", ""),
				new ConfigurationTemplate(UUID.randomUUID(), "content6 ${f} from mesh", "name6", "")));

		Map<String, String> meshTemplateVariables = new HashMap<>();
		meshTemplateVariables.put("c", "c3");
		meshTemplateVariables.put("f", "f3");
		c.setTemplateVariables(meshTemplateVariables);

		final List<ConfigurationTemplate> calculatedConfigurationTemplates = c.getCalculatedConfigurationTemplates();
		assertThat(calculatedConfigurationTemplates, hasSize(6));
		assertThat(calculatedConfigurationTemplates.get(0).getName(), is("name1"));
		assertThat(calculatedConfigurationTemplates.get(0).getContent(), is("content1 a1"));

		assertThat(calculatedConfigurationTemplates.get(1).getName(), is("name2"));
		assertThat(calculatedConfigurationTemplates.get(1).getContent(), is("content2 b2 from mesh"));

		assertThat(calculatedConfigurationTemplates.get(2).getName(), is("name3"));
		assertThat(calculatedConfigurationTemplates.get(2).getContent(), is("content3 c3 from version"));

		assertThat(calculatedConfigurationTemplates.get(3).getName(), is("name4"));
		assertThat(calculatedConfigurationTemplates.get(3).getContent(), is("content4 d1 from mesh"));

		assertThat(calculatedConfigurationTemplates.get(4).getName(), is("name5"));
		assertThat(calculatedConfigurationTemplates.get(4).getContent(), is("content5 e2 from version"));

		assertThat(calculatedConfigurationTemplates.get(5).getName(), is("name6"));
		assertThat(calculatedConfigurationTemplates.get(5).getContent(), is("content6 f3 from mesh"));
	}
}
