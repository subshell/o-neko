package io.oneko.namespace.rest;

import org.springframework.stereotype.Service;

import io.oneko.namespace.Namespace;

@Service
public class DefinedNamespaceDTOMapper {

	public DefinedNamespaceDTO namespaceToDTO(Namespace namespace) {
		DefinedNamespaceDTO dto = new DefinedNamespaceDTO();
		dto.setId(namespace.getId());
		dto.setName(namespace.asKubernetesNameSpace());
		return dto;
	}

}
