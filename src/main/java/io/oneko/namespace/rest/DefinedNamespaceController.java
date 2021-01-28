package io.oneko.namespace.rest;

import io.oneko.configuration.Controllers;
import io.oneko.kubernetes.NamespaceManager;
import io.oneko.namespace.DefinedNamespaceRepository;
import io.oneko.namespace.ReadableDefinedNamespace;
import io.oneko.namespace.WritableDefinedNamespace;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(DefinedNamespaceController.PATH)
public class DefinedNamespaceController {

	public static final String PATH = Controllers.ROOT_PATH + "/namespace";

	private final DefinedNamespaceRepository namespaceRepository;
	private final DefinedNamespaceDTOMapper dtoMapper;
	private final NamespaceManager namespaceManager;

	public DefinedNamespaceController(DefinedNamespaceRepository namespaceRepository,
																		DefinedNamespaceDTOMapper dtoMapper,
																		NamespaceManager namespaceManager) {
		this.namespaceRepository = namespaceRepository;
		this.dtoMapper = dtoMapper;
		this.namespaceManager = namespaceManager;
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping
	List<DefinedNamespaceDTO> getAllDefinedNamespaces() {
		return namespaceRepository.getAll().stream()
				.map(dtoMapper::namespaceToDTO)
				.collect(Collectors.toList());
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PostMapping
	DefinedNamespaceDTO createNamespace(@RequestBody DefinedNamespaceDTO dto) {
		WritableDefinedNamespace newNamespace = new WritableDefinedNamespace(dto.getName());
		final ReadableDefinedNamespace definedNamespace = namespaceRepository.add(newNamespace);
		namespaceManager.createNamespaceAndAddImagePullSecrets(definedNamespace);
		return dtoMapper.namespaceToDTO(definedNamespace);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}")
	DefinedNamespaceDTO getNamespaceById(@PathVariable UUID id) {
		return namespaceRepository.getById(id)
				.map(dtoMapper::namespaceToDTO)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Namespace with id " + id + " not found"));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@DeleteMapping("/{id}")
	void deleteNamespace(@PathVariable UUID id) {
		namespaceRepository.getById(id).ifPresent(namespace -> {
			namespaceManager.deleteNamespace(namespace);
			namespaceRepository.remove(namespace);
		});
	}

}
