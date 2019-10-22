package io.oneko.templates.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.oneko.templates.ConfigurationTemplate;

@Service
public class ConfigurationTemplateDTOMapper {

	public ConfigurationTemplateDTO toDTO(ConfigurationTemplate template) {
		return ConfigurationTemplateDTO.builder()
				.id(template.getId())
				.name(template.getName())
				.content(template.getContent())
				.description(template.getDescription())
				.build();
	}

	public ConfigurationTemplate fromDTO(ConfigurationTemplateDTO templateDTO) {
		UUID uuid = templateDTO.getId() == null ? UUID.randomUUID() : templateDTO.getId();

		return ConfigurationTemplate.builder()
				.id(uuid)
				.name(templateDTO.getName())
				.content(templateDTO.getContent())
				.description(templateDTO.getDescription())
				.build();
	}

	public List<ConfigurationTemplate> updateFromDTOs(List<ConfigurationTemplate> templates, List<ConfigurationTemplateDTO> dtos) {
		List<ConfigurationTemplate> result = new ArrayList<>();
		for (ConfigurationTemplate template : templates) {
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
