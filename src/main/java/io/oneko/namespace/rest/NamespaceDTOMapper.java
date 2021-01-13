package io.oneko.namespace.rest;

import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.DefinedNamespaceRepository;
import io.oneko.namespace.Namespace;
import io.oneko.namespace.WritableHasNamespace;
import org.springframework.stereotype.Service;

import java.util.Objects;

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

	public <T extends WritableHasNamespace> Namespace updateNamespaceOfOwner(T owner, NamespaceDTO namespaceDTO) {
		if (namespaceDTO != null && !Objects.equals(owner.getDefinedNamespaceId(), namespaceDTO.getId())) {
			if (namespaceDTO.getId() != null) {
				final var repository = namespaceRepository.getById(namespaceDTO.getId());
				repository.ifPresent(owner::assignDefinedNamespace);

				return repository.orElseThrow();
			}

			return owner.resetToImplicitNamespace();
		}

		return owner.getNamespace();
	}
}
