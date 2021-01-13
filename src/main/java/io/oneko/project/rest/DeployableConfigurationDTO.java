package io.oneko.project.rest;

import io.oneko.templates.rest.ConfigurationTemplateDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Data
public class DeployableConfigurationDTO {
	private String name;
	private Map<String, String> availableTemplateVariables;
	private List<ConfigurationTemplateDTO> configurationTemplates;
}
