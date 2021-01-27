package io.oneko.helm.rest;

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
import io.oneko.helm.HelmRegistryException;
import io.oneko.helm.HelmRegistryMapper;
import io.oneko.helm.HelmRegistryRepository;
import io.oneko.helm.ReadableHelmRegistry;
import io.oneko.helm.WritableHelmRegistry;
import io.oneko.helm.util.HelmRegistryCommandUtils;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ReadableProject;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(HelmRegistryController.PATH)
public class HelmRegistryController {
	public static final String PATH = Controllers.ROOT_PATH + "/helm/registries";
	private final HelmRegistryRepository helmRegistryRepository;
	private final ProjectRepository projectRepository;
	private final HelmRegistryMapper mapper;

	public HelmRegistryController(HelmRegistryRepository helmRegistryRepository,
																ProjectRepository projectRepository,
																HelmRegistryMapper mapper) {
		this.helmRegistryRepository = helmRegistryRepository;
		this.projectRepository = projectRepository;
		this.mapper = mapper;
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping
	List<HelmRegistryDTO> getAllRegistries() {
		return helmRegistryRepository.getAll().stream()
				.map(mapper::toHelmRegistryDTO)
				.collect(Collectors.toList());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	HelmRegistryDTO createRegistry(@RequestBody CreateHelmRegistryDTO dto) throws HelmRegistryException {
		WritableHelmRegistry registry = mapper.createRegistryFromDTO(dto);
		HelmRegistryCommandUtils.addRegistry(registry.readable());

		final ReadableHelmRegistry persistedRegistry = helmRegistryRepository.add(registry);
		return mapper.toHelmRegistryDTO(persistedRegistry);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}")
	HelmRegistryDTO getRegistryById(@PathVariable UUID id) {
		ReadableHelmRegistry reg = getRegistryOr404(id);
		return mapper.toHelmRegistryDTO(reg);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{id}")
	HelmRegistryDTO updateRegistry(@PathVariable UUID id, @RequestBody HelmRegistryDTO dto) throws HelmRegistryException {
		ReadableHelmRegistry registry = getRegistryOr404(id);
		HelmRegistryCommandUtils.addRegistry(registry);
		WritableHelmRegistry updatedRegistry = mapper.updateRegistryFromDTO(registry.writable(), dto);
		ReadableHelmRegistry persistedReg = helmRegistryRepository.add(updatedRegistry);
		return mapper.toHelmRegistryDTO(persistedReg);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	void deleteRegistry(@PathVariable UUID id) throws HelmRegistryException {
		ReadableHelmRegistry registry = getRegistryOr404(id);

		// TODO: remove registry only if it is not used anymore
		HelmRegistryCommandUtils.deleteRegistry(registry);
		helmRegistryRepository.remove(registry);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{id}/password")
	HelmRegistryDTO changeRegistryPassword(@PathVariable UUID id, @RequestBody ChangeHelmRegistryPasswordDTO dto) throws HelmRegistryException {
		ReadableHelmRegistry registry = getRegistryOr404(id);
		HelmRegistryCommandUtils.addRegistry(registry);

		WritableHelmRegistry writable = registry.writable();
		writable.setPassword(dto.getPassword());
		ReadableHelmRegistry persisted = helmRegistryRepository.add(writable);
		return mapper.toHelmRegistryDTO(persisted);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}/projects")
	List<String> getProjectsUsingRegistry(@PathVariable UUID id) {
		// TODO create helm method getByHelmRegistryUuid
		return this.projectRepository.getByDockerRegistryUuid(id)
				.stream()
				.map(ReadableProject::getName)
				.collect(Collectors.toList());
	}

	private ReadableHelmRegistry getRegistryOr404(UUID id) {
		return this.helmRegistryRepository.getById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Helm Registry with id " + id + "not found."));
	}
}
