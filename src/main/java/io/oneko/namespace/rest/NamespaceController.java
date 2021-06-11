package io.oneko.namespace.rest;

import static net.logstash.logback.argument.StructuredArguments.*;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.oneko.configuration.Controllers;
import io.oneko.kubernetes.NamespaceManager;
import io.oneko.namespace.NamespaceRepository;
import io.oneko.namespace.ReadableNamespace;
import io.oneko.namespace.WritableNamespace;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(NamespaceController.PATH)
public class NamespaceController {

	public static final String PATH = Controllers.ROOT_PATH + "/namespace";

	private final NamespaceRepository namespaceRepository;
	private final DefinedNamespaceDTOMapper dtoMapper;
	private final NamespaceManager namespaceManager;

	public NamespaceController(NamespaceRepository namespaceRepository,
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
		WritableNamespace newNamespace = new WritableNamespace(dto.getName());
		final ReadableNamespace definedNamespace = namespaceRepository.add(newNamespace);
		try {
			namespaceManager.createNamespaceAndAddImagePullSecrets(definedNamespace);
			return dtoMapper.namespaceToDTO(definedNamespace);
		} catch (KubernetesClientException e) {
			log.error("failed to create namespace in kubernetes ({})", kv("namespace", dto.getName()));
			namespaceRepository.remove(definedNamespace); // rollback
			throw e;
		}
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
