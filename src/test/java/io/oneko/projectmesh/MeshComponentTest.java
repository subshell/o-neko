package io.oneko.projectmesh;

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

import org.junit.jupiter.api.Test;

import io.oneko.docker.ReadableDockerRegistry;
import io.oneko.docker.WritableDockerRegistry;
import io.oneko.project.ProjectVersion;
import io.oneko.project.ReadableProject;
import io.oneko.project.WritableProject;
import io.oneko.project.WritableProjectVersion;
import io.oneko.project.WritableTemplateVariable;
import io.oneko.templates.WritableConfigurationTemplate;

class MeshComponentTest {

	@Test
	void testChangeVersion() {
		ReadableDockerRegistry reg = new WritableDockerRegistry().readable();
		WritableProject p = new WritableProject(reg.getId());
		final ProjectVersion v1 = p.createVersion("v1");
		final ProjectVersion v2 = p.createVersion("v2");
		final ReadableProject readable = p.readable();

		WritableProject someOtherProject = new WritableProject(reg.getId());
		someOtherProject.createVersion("v2");
		final ReadableProject someOtherReadable = someOtherProject.readable();

		WritableProjectMesh mesh = new WritableProjectMesh();
		WritableMeshComponent c = new WritableMeshComponent(mesh, readable, readable.getVersionByName("v1").get());

		assertThat(c.getProjectVersion().getId(), is(v1.getId()));

		c.setProjectVersion(readable.getVersionByName("v2").get());
		assertThat(c.getProjectVersion().getId(), is(v2.getId()));

		try {
			c.setProjectVersion(someOtherReadable.getVersionByName("v2").get());
			fail();
		} catch (IllegalArgumentException e) {
			assertThat(c.getProjectVersion().getId(), is(v2.getId()));
		}
	}

	@Test
	void testTemplateComposition() {
		ReadableDockerRegistry reg = new WritableDockerRegistry().readable();
		WritableProject p = new WritableProject(reg.getId());
		p.setDefaultConfigurationTemplates(Arrays.asList(
				new WritableConfigurationTemplate(UUID.randomUUID(), "content1 ${a}", "name1", ""),
				new WritableConfigurationTemplate(UUID.randomUUID(), "content2 ${b}", "name2", ""),
				new WritableConfigurationTemplate(UUID.randomUUID(), "content3 ${c}", "name3", ""),
				new WritableConfigurationTemplate(UUID.randomUUID(), "content4 ${d}", "name4", "")));
		List<WritableTemplateVariable> defaultTemplateVariables = new ArrayList<>();
		defaultTemplateVariables.add(new WritableTemplateVariable("a", "a", Collections.singletonList("a1"), true, "a1", false));
		defaultTemplateVariables.add(new WritableTemplateVariable("b", "b", Collections.singletonList("b1"), true, "b1", false));
		defaultTemplateVariables.add(new WritableTemplateVariable("c", "b", Collections.singletonList("c1"), true, "c1", false));
		defaultTemplateVariables.add(new WritableTemplateVariable("d", "b", Collections.singletonList("d1"), true, "d1", false));
		p.setTemplateVariables(defaultTemplateVariables);

		final WritableProjectVersion v = p.createVersion("v1");
		v.setConfigurationTemplates(Arrays.asList(
				new WritableConfigurationTemplate(UUID.randomUUID(), "content3 ${c} from version", "name3", ""),
				new WritableConfigurationTemplate(UUID.randomUUID(), "content4 ${d} from version", "name4", ""),
				new WritableConfigurationTemplate(UUID.randomUUID(), "content5 ${e} from version", "name5", "")));

		Map<String, String> templateVariables = new HashMap<>();
		templateVariables.put("b", "b2");
		templateVariables.put("e", "e2");
		v.setTemplateVariables(templateVariables);

		final ReadableProject readable = p.readable();

		WritableProjectMesh mesh = new WritableProjectMesh();
		WritableMeshComponent c = new WritableMeshComponent(mesh, readable, readable.getVersionByName(v.getName()).get());
		c.setConfigurationTemplates(Arrays.asList(
				new WritableConfigurationTemplate(UUID.randomUUID(), "content2 ${b} from mesh", "name2", ""),
				new WritableConfigurationTemplate(UUID.randomUUID(), "content4 ${d} from mesh", "name4", ""),
				new WritableConfigurationTemplate(UUID.randomUUID(), "content6 ${f} from mesh", "name6", "")));

		Map<String, String> meshTemplateVariables = new HashMap<>();
		meshTemplateVariables.put("c", "c3");
		meshTemplateVariables.put("f", "f3");
		c.setTemplateVariables(meshTemplateVariables);

		final List<WritableConfigurationTemplate> calculatedConfigurationTemplates = c.getCalculatedConfigurationTemplates();
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
