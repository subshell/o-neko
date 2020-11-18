package io.oneko.project.rest.export;

import io.oneko.project.rest.ProjectDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

@Mapper
public interface ProjectExportDTOMapper {
	ProjectExportDTOMapper MAPPER = Mappers.getMapper(ProjectExportDTOMapper.class);

	ProjectExportDTO toProjectExportDto(ProjectDTO projectDTO);

	@AfterMapping
	default void setMetadata(ProjectDTO projectDTO, @MappingTarget ProjectExportDTO dto) {
		// don't export the uuid references
		dto.getDefaultConfigurationTemplates().forEach(configurationTemplateDTO -> configurationTemplateDTO.setId(null));
		dto.getTemplateVariables().forEach(templateVariableDTO -> templateVariableDTO.setId(null));

		dto.setExportMetadata(ProjectExportMetadataDTO.builder()
				.version(0)
				.exportedAt(Instant.now())
				.build());
	}
}
