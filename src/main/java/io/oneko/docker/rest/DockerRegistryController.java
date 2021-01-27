package io.oneko.docker.rest;

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
import io.oneko.docker.DockerRegistryRepository;
import io.oneko.docker.ReadableDockerRegistry;
import io.oneko.docker.WritableDockerRegistry;
import io.oneko.docker.v2.DockerRegistryClientFactory;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ReadableProject;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(DockerRegistryController.PATH)
public class DockerRegistryController {

	public static final String PATH = Controllers.ROOT_PATH + "/dockerRegistry";

	private final DockerRegistryRepository dockerRegistryRepository;
	private final ProjectRepository projectRepository;
	private final DockerRegistryDTOMapper dtoMapper;
	private final DockerRegistryClientFactory clientFactory;

	public DockerRegistryController(DockerRegistryRepository dockerRegistryRepository, ProjectRepository projectRepository, DockerRegistryDTOMapper dtoMapper, DockerRegistryClientFactory clientFactory) {
		this.dockerRegistryRepository = dockerRegistryRepository;
		this.projectRepository = projectRepository;
		this.dtoMapper = dtoMapper;
		this.clientFactory = clientFactory;
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping
	List<DockerRegistryDTO> getAllRegistries() {
		return dockerRegistryRepository.getAll().stream()
				.map(dtoMapper::registryToDTO)
				.collect(Collectors.toList());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	DockerRegistryDTO createRegistry(@RequestBody DockerRegistryDTO dto) {
		WritableDockerRegistry registry = dtoMapper.updateRegistryFromDTO(new WritableDockerRegistry(), dto);
		final ReadableDockerRegistry persistedRegistry = dockerRegistryRepository.add(registry);
		return dtoMapper.registryToDTO(persistedRegistry);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}")
	DockerRegistryDTO getRegistryById(@PathVariable UUID id) {
		ReadableDockerRegistry reg = getRegistryOr404(id);
		return dtoMapper.registryToDTO(reg);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{id}")
	DockerRegistryDTO updateRegistry(@PathVariable UUID id, @RequestBody DockerRegistryDTO dto) {
		ReadableDockerRegistry reg = getRegistryOr404(id);
		WritableDockerRegistry updatedRegistry = dtoMapper.updateRegistryFromDTO(reg.writable(), dto);
		ReadableDockerRegistry persistedReg = dockerRegistryRepository.add(updatedRegistry);
		return dtoMapper.registryToDTO(persistedReg);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	void deleteRegistry(@PathVariable UUID id) {
		ReadableDockerRegistry reg = getRegistryOr404(id);
		dockerRegistryRepository.remove(reg);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{id}/password")
	DockerRegistryDTO changeRegistryPassword(@PathVariable UUID id, @RequestBody ChangeDockerRegistryPasswordDTO dto) {
		ReadableDockerRegistry reg = getRegistryOr404(id);
		WritableDockerRegistry writable = reg.writable();
		writable.setPassword(dto.getPassword());
		ReadableDockerRegistry persisted = dockerRegistryRepository.add(writable);
		return dtoMapper.registryToDTO(persisted);
	}

	/**
	 * Not used as part of the frontend, but handy for internal testing...
	 */
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/{id}/availability")
	DockerRegistryAPICheckDTO checkRegistryAccess(@PathVariable UUID id) {
		ReadableDockerRegistry reg = getRegistryOr404(id);
		try {
			return DockerRegistryAPICheckDTO.okay(clientFactory.checkRegistryAvailability(reg));
		} catch (Exception e) {
			return DockerRegistryAPICheckDTO.error(e.getMessage());
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}/projects")
	List<String> getProjectsUsingRegistry(@PathVariable UUID id) {
		return this.projectRepository.getByDockerRegistryUuid(id)
				.stream()
				.map(ReadableProject::getName)
				.collect(Collectors.toList());
	}

	private ReadableDockerRegistry getRegistryOr404(UUID id) {
		return this.dockerRegistryRepository.getById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Docker Registry with id " + id + "not found."));
	}

}
