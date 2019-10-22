package io.oneko.namespace.rest;

import java.util.UUID;

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
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.DefinedNamespaceRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
	Flux<DefinedNamespaceDTO> getAllDefinedNamespaces() {
		return this.namespaceRepository.getAll().map(this.dtoMapper::namespaceToDTO);
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@PostMapping
	Mono<DefinedNamespaceDTO> createNamespace(@RequestBody DefinedNamespaceDTO dto) {
		DefinedNamespace newNamespace = new DefinedNamespace(dto.getName());
		return namespaceRepository.add(newNamespace).map(this.dtoMapper::namespaceToDTO);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}")
	Mono<DefinedNamespaceDTO> getNamespaceById(@PathVariable UUID id) {
		return this.namespaceRepository.getById(id)
				.map(this.dtoMapper::namespaceToDTO)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Namespace with id " + id + " not found")));
	}

	@PreAuthorize("hasAnyRole('ADMIN')")
	@DeleteMapping("/{id}")
	Mono<Void> deleteNamespace(@PathVariable UUID id) {
		return this.namespaceRepository.getById(id).flatMap(this.namespaceRepository::remove);
	}

}
