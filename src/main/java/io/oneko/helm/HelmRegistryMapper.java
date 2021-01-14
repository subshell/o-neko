package io.oneko.helm;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;

import io.oneko.helm.rest.CreateHelmRegistryDTO;
import io.oneko.helm.rest.HelmRegistryDTO;


@Mapper(componentModel = "spring")
public abstract class HelmRegistryMapper {
	public abstract HelmRegistryDTO toHelmRegistryDTO(HelmRegistry helmRegistry);

	public WritableHelmRegistry updateRegistryFromDTO(WritableHelmRegistry registry, HelmRegistryDTO dto) {
		cleanup(dto);

		//id can not be changed
		registry.setName(dto.getName());
		registry.setUrl(dto.getUrl());
		registry.setUsername(dto.getUsername());
		return registry;
	}

	public WritableHelmRegistry createRegistryFromDTO(CreateHelmRegistryDTO dto) {
		WritableHelmRegistry registry = new WritableHelmRegistry();
		cleanup(dto);

		//id can not be changed
		registry.setName(dto.getName());
		registry.setUrl(dto.getUrl());
		registry.setUsername(dto.getUsername());
		registry.setPassword(dto.getPassword());
		return registry;
	}

	protected void cleanup(HelmRegistryDTO dto) {
		dto.setName(StringUtils.replace(StringUtils.trim(dto.getName()), " ", "_"));
		dto.setUrl(StringUtils.trim(dto.getUrl()));
		dto.setUsername(StringUtils.trim(dto.getUsername()));
	}

}
