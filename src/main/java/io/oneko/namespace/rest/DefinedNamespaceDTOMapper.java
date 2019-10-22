package io.oneko.namespace.rest;

import org.springframework.stereotype.Service;

import io.oneko.namespace.DefinedNamespace;

@Service
public class DefinedNamespaceDTOMapper {

	public DefinedNamespaceDTO namespaceToDTO(DefinedNamespace namespace) {
		DefinedNamespaceDTO dto = new DefinedNamespaceDTO();
		dto.setId(namespace.getId());
		dto.setName(namespace.asKubernetesNameSpace());
		return dto;
	}

}
