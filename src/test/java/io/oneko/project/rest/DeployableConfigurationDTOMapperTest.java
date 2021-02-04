package io.oneko.project.rest;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.Test;

import io.oneko.project.WritableProject;
import io.oneko.project.WritableProjectVersion;
import io.oneko.templates.WritableConfigurationTemplate;
import io.oneko.templates.rest.ConfigurationTemplateDTOMapper;

class DeployableConfigurationDTOMapperTest {

	@Test
	void configShouldHaveReplacesVariables() {
		ConfigurationTemplateDTOMapper templateDTOMapper = new ConfigurationTemplateDTOMapper();
		DeployableConfigurationDTOMapper mapper = new DeployableConfigurationDTOMapper(templateDTOMapper);

		Map<String, String> templateVariables = new HashedMap<>();
		templateVariables.put("VAR_1", "1");
		templateVariables.put("VAR_2", "2");

		final WritableProject project1 = WritableProject.builder()
				.versions(Collections.emptyList())
				.name("project1")
				.id(UUID.randomUUID())
				.templateVariables(new ArrayList<>())
				.build();
		final WritableProjectVersion version = project1.createVersion("Name");
		version.setConfigurationTemplates(Collections.singletonList(
				WritableConfigurationTemplate.builder().content("{{VAR_1}} {{VAR_2}}").build()
		));
		version.setTemplateVariables(templateVariables);

		assertThat(mapper.create(version).getConfigurationTemplates().get(0).getContent(), is("1 2"));
	}

}
