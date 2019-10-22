package io.oneko.project.rest;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.Test;

import io.oneko.project.Project;
import io.oneko.project.ProjectVersion;
import io.oneko.templates.ConfigurationTemplate;
import io.oneko.templates.rest.ConfigurationTemplateDTOMapper;

public class DeployableConfigurationDTOMapperTest {

	@Test
	public void configShouldHaveReplacesVariables() {
		ConfigurationTemplateDTOMapper templateDTOMapper = new ConfigurationTemplateDTOMapper();
		DeployableConfigurationDTOMapper mapper = new DeployableConfigurationDTOMapper(templateDTOMapper);

		Map<String, String> templateVariables = new HashedMap<>();
		templateVariables.put("VAR_1", "1");
		templateVariables.put("VAR_2", "2");

		ProjectVersion projectVersion = ProjectVersion.builder()
				.uuid(UUID.randomUUID())
				.name("Name")
				.project(Project.builder()
						.name("project1")
						.uuid(UUID.randomUUID())
						.templateVariables(new ArrayList<>())
						.build())
				.configurationTemplates(Collections.singletonList(
						ConfigurationTemplate.builder().content("${VAR_1} ${VAR_2}").build()
				))
				.templateVariables(templateVariables)
				.build();

		assertThat(mapper.create(projectVersion).getConfigurationTemplates().get(0).getContent(), is("1 2"));
	}

}