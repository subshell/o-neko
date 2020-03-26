package io.oneko.project.rest;

import java.util.Objects;
import java.util.Optional;
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
import io.oneko.docker.DockerRegistryRepository;
import io.oneko.docker.ReadableDockerRegistry;
import io.oneko.kubernetes.KubernetesDeploymentManager;
import io.oneko.project.ReadableProject;
import io.oneko.project.WritableProject;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.project.rest.export.ProjectExportDTO;
import io.oneko.project.rest.export.ProjectExportDTOMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(ProjectController.PATH)
public class ProjectController {

	public static final String PATH = Controllers.ROOT_PATH + "/project";

	private final ProjectRepository projectRepository;
	private final DockerRegistryRepository dockerRegistryRepository;
	private final ProjectDTOMapper dtoMapper;
	private final DeployableConfigurationDTOMapper configurationDTOMapper;
	private final KubernetesDeploymentManager kubernetesDeploymentManager;

	public ProjectController(ProjectRepository projectRepository, DockerRegistryRepository dockerRegistryRepository,
	                         ProjectDTOMapper dtoMapper,
	                         DeployableConfigurationDTOMapper configurationDTOMapper,
	                         KubernetesDeploymentManager kubernetesDeploymentManager) {
		this.projectRepository = projectRepository;
		this.dockerRegistryRepository = dockerRegistryRepository;
		this.dtoMapper = dtoMapper;
		this.configurationDTOMapper = configurationDTOMapper;
		this.kubernetesDeploymentManager = kubernetesDeploymentManager;
	}

	private Mono<ReadableDockerRegistry> getDockerRegistryForProject(ReadableProject project, ProjectDTO dto) {
		if (Objects.equals(project.getDockerRegistryUuid(), dto.getDockerRegistryUUID())) {
			//no change, so...
			return Mono.just(project.getDockerRegistry());
		} else if (Objects.isNull(dto.getDockerRegistryUUID())) {
			return Mono.empty();
		} else {
			return dockerRegistryRepository.getById(dto.getDockerRegistryUUID())
					.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "DockerRegistry with id " + dto.getDockerRegistryUUID() + " not found")));
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@GetMapping
	Flux<ProjectDTO> getAllProjects() {
		return this.projectRepository.getAll().flatMap(this.dtoMapper::projectToDTO);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@PostMapping
	Mono<ProjectDTO> createProject(@RequestBody ProjectDTO dto) {
		return dockerRegistryRepository.getById(dto.getDockerRegistryUUID())
				.map(WritableProject::new)
				.flatMap(p -> this.dtoMapper.updateProjectFromDTO(p, dto, p.getDockerRegistry()))
				.flatMap(this.projectRepository::add)
				.flatMap(this.dtoMapper::projectToDTO);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@GetMapping("/{id}")
	Mono<ProjectDTO> getProjectById(@PathVariable UUID id) {
		return this.projectRepository.getById(id)
				.flatMap(this.dtoMapper::projectToDTO)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id " + id + " not found")));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@PostMapping("/{id}")
	Mono<ProjectDTO> updateProject(@PathVariable UUID id, @RequestBody ProjectDTO dto) {
		return this.projectRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id " + id + " not found")))
				.zipWhen(project -> this.getDockerRegistryForProject(project, dto))
				.flatMap(tuple2 -> this.dtoMapper.updateProjectFromDTO(tuple2.getT1().writable(), dto, tuple2.getT2()))
				.flatMap(this.projectRepository::add)
				.flatMap(this.dtoMapper::projectToDTO);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@DeleteMapping("/{id}")
	Mono<Void> deleteProject(@PathVariable UUID id) {
		return this.projectRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id " + id + " not found")))
				.flatMap(this.projectRepository::remove);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@GetMapping("/{id}/export")
	Mono<ProjectExportDTO> exportProject(@PathVariable UUID id) {
		return this.projectRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id " + id + " not found")))
				.flatMap(this.dtoMapper::projectToDTO)
				.map(ProjectExportDTOMapper.MAPPER::toProjectExportDto);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@PostMapping("/{id}/version/{versionId}/deploy")
	Mono<ProjectDTO> triggerDeploymentOfVersion(@PathVariable UUID id, @PathVariable UUID versionId) {
		return this.projectRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id " + id + " not found")))
				.map(ReadableProject::writable)
				.map(project -> project.getVersionByUUID(versionId))
				.filter(Optional::isPresent)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project version with id " + versionId + " not found")))
				.flatMap(version -> kubernetesDeploymentManager.deploy(version.get()))
				.map(ProjectVersion::getProject)
				.flatMap(dtoMapper::projectToDTO);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@PostMapping("/{id}/version/{versionId}/stop")
	Mono<Void> stopDeployment(@PathVariable UUID id, @PathVariable UUID versionId) {
		return this.projectRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id " + id + " not found")))
				.map(ReadableProject::writable)
				.map(project -> project.getVersionByUUID(versionId))
				.filter(Optional::isPresent)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project version with id " + versionId + " not found")))
				.map(Optional::get)
				.flatMap(kubernetesDeploymentManager::stopDeployment)
				.then();
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}/version/{versionId}/configuration")
	Mono<DeployableConfigurationDTO> showCalculatedConfigurationOfVersion(@PathVariable UUID id, @PathVariable UUID versionId) {
		return this.projectRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id " + id + " not found")))
				.map(project -> project.getVersionByUUID(versionId))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Project version with id " + versionId + " not found")))
				.map(this.configurationDTOMapper::create);
	}

}
