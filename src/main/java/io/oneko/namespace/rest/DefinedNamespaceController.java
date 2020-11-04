package io.oneko.namespace.rest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.oneko.configuration.Controllers;
import io.oneko.namespace.DefinedNamespaceRepository;
import io.oneko.namespace.ReadableDefinedNamespace;
import io.oneko.namespace.WritableDefinedNamespace;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(DefinedNamespaceController.PATH)
public class DefinedNamespaceController {

	public static final String PATH = Controllers.ROOT_PATH + "/namespace";

	private final DefinedNamespaceRepository namespaceRepository;
	private final DefinedNamespaceDTOMapper dtoMapper;

	public DefinedNamespaceController(DefinedNamespaceRepository namespaceRepository, DefinedNamespaceDTOMapper dtoMapper) {
		this.namespaceRepository = namespaceRepository;
		this.dtoMapper = dtoMapper;
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping
	List<DefinedNamespaceDTO> getAllDefinedNamespaces() {
		return this.namespaceRepository.getAll().stream()
				.map(this.dtoMapper::namespaceToDTO)
				.collect(Collectors.toList());
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PostMapping
	DefinedNamespaceDTO createNamespace(@RequestBody DefinedNamespaceDTO dto) {
		WritableDefinedNamespace newNamespace = new WritableDefinedNamespace(dto.getName());
		final ReadableDefinedNamespace definedNamespace = namespaceRepository.add(newNamespace);
		return dtoMapper.namespaceToDTO(definedNamespace);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}")
	DefinedNamespaceDTO getNamespaceById(@PathVariable UUID id) {
		return this.namespaceRepository.getById(id)
				.map(this.dtoMapper::namespaceToDTO)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Namespace with id " + id + " not found"));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@DeleteMapping("/{id}")
	void deleteNamespace(@PathVariable UUID id) {
		this.namespaceRepository.getById(id).ifPresent(this.namespaceRepository::remove);
	}

}
