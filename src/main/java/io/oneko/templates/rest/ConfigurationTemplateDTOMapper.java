package io.oneko.templates.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.oneko.templates.WritableConfigurationTemplate;

@Service
public class ConfigurationTemplateDTOMapper {

	public ConfigurationTemplateDTO toDTO(WritableConfigurationTemplate template) {
		return ConfigurationTemplateDTO.builder()
				.id(template.getId())
				.name(template.getName())
				.content(template.getContent())
				.description(template.getDescription())
				.build();
	}

	public WritableConfigurationTemplate fromDTO(ConfigurationTemplateDTO templateDTO) {
		UUID uuid = templateDTO.getId() == null ? UUID.randomUUID() : templateDTO.getId();

		return WritableConfigurationTemplate.builder()
				.id(uuid)
				.name(templateDTO.getName())
				.content(templateDTO.getContent())
				.description(templateDTO.getDescription())
				.build();
	}

	public List<WritableConfigurationTemplate> updateFromDTOs(List<WritableConfigurationTemplate> templates, List<ConfigurationTemplateDTO> dtos) {
		List<WritableConfigurationTemplate> result = new ArrayList<>();
		for (WritableConfigurationTemplate template : templates) {
			findInList(dtos, template.getId()).ifPresent(dto -> {
				template.setName(dto.getName());
				template.setDescription(dto.getDescription());
				template.setContent(dto.getContent());
				result.add(template);
			});
		}
		dtos.stream().filter(dto -> dto.getId() == null).forEach(dto -> result.add(fromDTO(dto)));
		return result;
	}

	private Optional<ConfigurationTemplateDTO> findInList(List<ConfigurationTemplateDTO> dtos, UUID uuid) {
		return dtos.stream()
				.filter(dto -> Objects.equals(dto.getId(), uuid))
				.findFirst();
	}
}
