package io.oneko.namespace.rest;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.DefinedNamespaceRepository;
import io.oneko.namespace.Namespace;
import io.oneko.namespace.WritableHasNamespace;

@Service
public class NamespaceDTOMapper {

	private final DefinedNamespaceRepository namespaceRepository;

	public NamespaceDTOMapper(DefinedNamespaceRepository namespaceRepository) {
		this.namespaceRepository = namespaceRepository;
	}

	public NamespaceDTO namespaceToDTO(Namespace namespace) {
		NamespaceDTO dto = new NamespaceDTO();
		dto.setName(namespace.asKubernetesNameSpace());
		if (namespace instanceof DefinedNamespace) {
			dto.setId(((DefinedNamespace) namespace).getId());
		}
		return dto;
	}

	public <T extends WritableHasNamespace> void updateNamespaceOfOwner(T owner, NamespaceDTO namespaceDTO) {
		if (namespaceDTO != null && !Objects.equals(owner.getDefinedNamespaceId(), namespaceDTO.getId())) {
			if (namespaceDTO.getId() != null) {
				namespaceRepository.getById(namespaceDTO.getId()).ifPresent(namespace -> owner.assignDefinedNamespace(namespace));
			} else {
				owner.resetToImplicitNamespace();
			}
		}
	}
}
