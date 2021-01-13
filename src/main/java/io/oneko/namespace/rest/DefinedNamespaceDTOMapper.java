package io.oneko.namespace.rest;

import io.oneko.namespace.DefinedNamespace;
import org.springframework.stereotype.Service;

@Service
public class DefinedNamespaceDTOMapper {

	public DefinedNamespaceDTO namespaceToDTO(DefinedNamespace namespace) {
		DefinedNamespaceDTO dto = new DefinedNamespaceDTO();
		dto.setId(namespace.getId());
		dto.setName(namespace.asKubernetesNameSpace());
		return dto;
	}

}
