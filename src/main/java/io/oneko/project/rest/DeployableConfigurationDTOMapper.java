package io.oneko.project.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.oneko.project.ProjectVersion;
import io.oneko.templates.rest.ConfigurationTemplateDTO;
import io.oneko.templates.rest.ConfigurationTemplateDTOMapper;

@Service
public class DeployableConfigurationDTOMapper {

	private final ConfigurationTemplateDTOMapper configurationTemplateDTOMapper;

	@Autowired
	public DeployableConfigurationDTOMapper(ConfigurationTemplateDTOMapper configurationTemplateDTOMapper) {
		this.configurationTemplateDTOMapper = configurationTemplateDTOMapper;
	}

	public DeployableConfigurationDTO create(ProjectVersion<?, ?> version) {
		final List<ConfigurationTemplateDTO> templateDTOs = version.getCalculatedConfigurationTemplates().stream()
				.map(configurationTemplateDTOMapper::toDTO)
				.collect(Collectors.toList());

		DeployableConfigurationDTO dto = new DeployableConfigurationDTO();
		dto.setName(version.getName());
		dto.setConfigurationTemplates(templateDTOs);
		Map<String, String> effectiveTemplateVariables = new HashMap<>();
		for (Map.Entry<String, Object> entry : version.calculateEffectiveTemplateVariables().entrySet()) {
			if (entry.getValue() instanceof String) {
				effectiveTemplateVariables.put(entry.getKey(), (String) entry.getValue());
			} else if (entry.getValue() instanceof String[]) {
				String[] arr = (String[]) entry.getValue();
				for (int i = 0; i < arr.length; i++) {
					effectiveTemplateVariables.put(entry.getKey() + "[" + i + "]", arr[i]);
				}
			}
		}
		dto.setAvailableTemplateVariables(effectiveTemplateVariables);
		return dto;
	}
}
