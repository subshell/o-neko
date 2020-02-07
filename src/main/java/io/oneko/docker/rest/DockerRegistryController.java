package io.oneko.docker.rest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.oneko.docker.WritableDockerRegistry;
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
import io.oneko.docker.DockerRegistry;
import io.oneko.docker.DockerRegistryRepository;
import io.oneko.docker.v2.DockerRegistryV2ClientFactory;
import io.oneko.project.Project;
import io.oneko.project.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(DockerRegistryController.PATH)
public class DockerRegistryController {

	public static final String PATH = Controllers.ROOT_PATH + "/dockerRegistry";

	private final DockerRegistryRepository dockerRegistryRepository;
	private final ProjectRepository projectRepository;
	private final DockerRegistryDTOMapper dtoMapper;
	private final DockerRegistryV2ClientFactory clientFactory;

	public DockerRegistryController(DockerRegistryRepository dockerRegistryRepository, ProjectRepository projectRepository, DockerRegistryDTOMapper dtoMapper, DockerRegistryV2ClientFactory clientFactory) {
		this.dockerRegistryRepository = dockerRegistryRepository;
		this.projectRepository = projectRepository;
		this.dtoMapper = dtoMapper;
		this.clientFactory = clientFactory;
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping
	Flux<DockerRegistryDTO> getAllRegistries() {
		return this.dockerRegistryRepository.getAll().map(this.dtoMapper::registryToDTO);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping
	Mono<DockerRegistryDTO> createRegistry(@RequestBody DockerRegistryDTO dto) {
		WritableDockerRegistry registry = new WritableDockerRegistry();
		registry = this.dtoMapper.updateRegistryFromDTO(registry, dto);
		return this.dockerRegistryRepository.add(registry)
				.map(this.dtoMapper::registryToDTO);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}")
	Mono<DockerRegistryDTO> getRegistryById(@PathVariable UUID id) {
		return this.dockerRegistryRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "DockerRegistry with id " + id + " not found")))
				.map(this.dtoMapper::registryToDTO);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{id}")
	Mono<DockerRegistryDTO> updateRegistry(@PathVariable UUID id, @RequestBody DockerRegistryDTO dto) {
		return this.dockerRegistryRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "DockerRegistry with id " + id + " not found")))
				.map(DockerRegistry::writable)
				.map(p -> this.dtoMapper.updateRegistryFromDTO(p, dto))
				.flatMap(this.dockerRegistryRepository::add)
				.map(this.dtoMapper::registryToDTO);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	Mono<Void> deleteRegistry(@PathVariable UUID id) {
		return this.dockerRegistryRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "DockerRegistry with id " + id + " not found")))
				.flatMap(this.dockerRegistryRepository::remove);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{id}/password")
	Mono<DockerRegistryDTO> changeRegistryPassword(@PathVariable UUID id, @RequestBody ChangeDockerRegistryPasswordDTO dto) {
		return this.dockerRegistryRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "DockerRegistry with id " + id + " not found")))
				.map(DockerRegistry::writable)
				.map(dockerRegistry -> {
					dockerRegistry.setPassword(dto.getPassword());
					return dockerRegistry;
				})
				.flatMap(this.dockerRegistryRepository::add)
				.map(this.dtoMapper::registryToDTO);
	}

	/**
	 * Not used as part of the frontend, but handy for internal testing...
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}/availability")
	Mono<DockerRegistryAPICheckDTO> checkRegistryAccess(@PathVariable UUID id) {
		return this.dockerRegistryRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "DockerRegistry with id " + id + " not found")))
				.flatMap(this.clientFactory::checkRegistryAvailability)
				.map(DockerRegistryAPICheckDTO::okay)
				.onErrorResume(exception -> Mono.just(DockerRegistryAPICheckDTO.error(exception.getMessage())));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}/project")
	Mono<List<String>> getProjectsUsingRegistry(@PathVariable UUID id) {
		return this.projectRepository.getByDockerRegistryUuid(id)
				.map(Project::getName)
				.collect(Collectors.toList());
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}/imageNames")
	Mono<List<String>> getImageNamesFromRegistry(@PathVariable UUID id) {
		//TODO: this is somehow not working due to a lack of permissions...
		return this.dockerRegistryRepository.getById(id)
				.flatMap(reg -> this.clientFactory.getDockerRegistryClient(reg))
				.flatMap(client -> client.getAllImageNames());
	}

}
