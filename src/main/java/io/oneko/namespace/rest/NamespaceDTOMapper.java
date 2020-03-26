package io.oneko.namespace.rest;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.DefinedNamespaceRepository;
import io.oneko.namespace.Namespace;
import io.oneko.namespace.WritableHasNamespace;
import reactor.core.publisher.Mono;

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

	public <T extends WritableHasNamespace> Mono<T> updateNamespaceOfOwner(T owner, NamespaceDTO namespaceDTO) {
		if (namespaceDTO == null || Objects.equals(owner.getDefinedNamespaceId(), namespaceDTO.getId())) {
			return Mono.just(owner);
		}
		if (namespaceDTO.getId() != null) {
			return namespaceRepository.getById(namespaceDTO.getId())
					.map(namespace -> {
						owner.assignDefinedNamespace(namespace);
						return owner;
					});
		} else {
			owner.resetToImplicitNamespace();
			return Mono.just(owner);
		}
	}
}
