package io.oneko.project.rest;

import io.oneko.project.ProjectVersion;
import io.oneko.projectmesh.MeshComponent;
import io.oneko.projectmesh.MeshService;
import io.oneko.templates.rest.ConfigurationTemplateDTO;
import io.oneko.templates.rest.ConfigurationTemplateDTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeployableConfigurationDTOMapper {

	private final ConfigurationTemplateDTOMapper configurationTemplateDTOMapper;
	private final MeshService meshService;

	@Autowired
	public DeployableConfigurationDTOMapper(ConfigurationTemplateDTOMapper configurationTemplateDTOMapper, MeshService meshService) {
		this.configurationTemplateDTOMapper = configurationTemplateDTOMapper;
		this.meshService = meshService;
	}

	public DeployableConfigurationDTO create(ProjectVersion<?, ?> version) {
		final List<ConfigurationTemplateDTO> templateDTOs = version.getCalculatedConfigurationTemplates().stream()
				.map(configurationTemplateDTOMapper::toDTO)
				.collect(Collectors.toList());

		DeployableConfigurationDTO dto = new DeployableConfigurationDTO();
		dto.setName(version.getName());
		dto.setConfigurationTemplates(templateDTOs);
		dto.setAvailableTemplateVariables(version.calculateEffectiveTemplateVariables());
		return dto;
	}

	public DeployableConfigurationDTO create(MeshComponent<?, ?> component) {
		final List<ConfigurationTemplateDTO> templateDTOs = meshService.getCalculatedConfigurationTemplates(component).stream()
				.map(configurationTemplateDTOMapper::toDTO)
				.collect(Collectors.toList());

		DeployableConfigurationDTO dto = new DeployableConfigurationDTO();
		dto.setName(component.getName());
		dto.setConfigurationTemplates(templateDTOs);
		dto.setAvailableTemplateVariables(meshService.calculateEffectiveTemplateVariables(component));
		return dto;
	}
}
