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
import io.oneko.helm.HelmRegistryMapper;
import io.oneko.helm.HelmRegistryRepository;
import io.oneko.helm.ReadableHelmRegistry;
import io.oneko.helm.WritableHelmRegistry;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(HelmRegistryController.PATH)
public class HelmRegistryController {
	public static final String PATH = Controllers.ROOT_PATH + "/helm/registries";
	private HelmRegistryRepository helmRegistryRepository;
	private HelmRegistryMapper mapper;

	public HelmRegistryController(HelmRegistryRepository helmRegistryRepository, HelmRegistryMapper mapper) {
		this.helmRegistryRepository = helmRegistryRepository;
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
	HelmRegistryDTO createRegistry(@RequestBody CreateHelmRegistryDTO dto) {
		WritableHelmRegistry registry = mapper.createRegistryFromDTO(dto);
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
	HelmRegistryDTO updateRegistry(@PathVariable UUID id, @RequestBody HelmRegistryDTO dto) {
		ReadableHelmRegistry reg = getRegistryOr404(id);
		WritableHelmRegistry updatedRegistry = mapper.updateRegistryFromDTO(reg.writable(), dto);
		ReadableHelmRegistry persistedReg = helmRegistryRepository.add(updatedRegistry);
		return mapper.toHelmRegistryDTO(persistedReg);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	void deleteRegistry(@PathVariable UUID id) {
		ReadableHelmRegistry reg = getRegistryOr404(id);
		helmRegistryRepository.remove(reg);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{id}/password")
	HelmRegistryDTO changeRegistryPassword(@PathVariable UUID id, @RequestBody ChangeHelmRegistryPasswordDTO dto) {
		ReadableHelmRegistry reg = getRegistryOr404(id);
		WritableHelmRegistry writable = reg.writable();
		writable.setPassword(dto.getPassword());
		ReadableHelmRegistry persisted = helmRegistryRepository.add(writable);
		return mapper.toHelmRegistryDTO(persisted);
	}

	private ReadableHelmRegistry getRegistryOr404(UUID id) {
		return this.helmRegistryRepository.getById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Helm Registry with id " + id + "not found."));
	}
}
