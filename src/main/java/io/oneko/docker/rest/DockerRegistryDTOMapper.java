package io.oneko.docker.rest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import io.oneko.docker.DockerRegistry;

@Service
public class DockerRegistryDTOMapper {

	public DockerRegistryDTO registryToDTO(DockerRegistry registry) {
		DockerRegistryDTO dto = new DockerRegistryDTO();
		dto.setUuid(registry.getUuid());
		dto.setName(registry.getName());
		dto.setRegistryUrl(registry.getRegistryUrl());
		dto.setUserName(registry.getUserName());
		dto.setTrustInsecureCertificate(registry.isTrustInsecureCertificate());
		//we don't send the password
		return dto;
	}

	public DockerRegistry updateRegistryFromDTO(DockerRegistry registry, DockerRegistryDTO dto) {
		cleanupDTO(dto);

		//id can not be changed
		registry.setName(dto.getName());
		registry.setRegistryUrl(dto.getRegistryUrl());
		registry.setUserName(dto.getUserName());
		registry.setTrustInsecureCertificate(dto.isTrustInsecureCertificate());
		return registry;
	}

	private void cleanupDTO(DockerRegistryDTO dto) {
		dto.setName(StringUtils.replace(StringUtils.trim(dto.getName()), " ", "_"));
		dto.setRegistryUrl(StringUtils.trim(dto.getRegistryUrl()));
		dto.setUserName(StringUtils.trim(dto.getUserName()));
	}

}
