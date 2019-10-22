package io.oneko.projectmesh.rest;

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
import io.oneko.kubernetes.KubernetesDeploymentManager;
import io.oneko.project.rest.DeployableConfigurationDTO;
import io.oneko.project.rest.DeployableConfigurationDTOMapper;
import io.oneko.projectmesh.ProjectMesh;
import io.oneko.projectmesh.ProjectMeshRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(ProjectMeshController.PATH)
public class ProjectMeshController {

	public static final String PATH = Controllers.ROOT_PATH + "/projectMesh";

	private final ProjectMeshRepository meshRepository;
	private final ProjectMeshDTOMapper dtoMapper;
	private final DeployableConfigurationDTOMapper configurationDTOMapper;
	private final KubernetesDeploymentManager kubernetesDeploymentManager;

	public ProjectMeshController(ProjectMeshRepository meshRepository, ProjectMeshDTOMapper dtoMapper, DeployableConfigurationDTOMapper configurationDTOMapper, KubernetesDeploymentManager kubernetesDeploymentManager) {
		this.meshRepository = meshRepository;
		this.dtoMapper = dtoMapper;
		this.configurationDTOMapper = configurationDTOMapper;
		this.kubernetesDeploymentManager = kubernetesDeploymentManager;
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@GetMapping
	Flux<ProjectMeshDTO> getAllProjectMeshes() {
		return this.meshRepository.getAll().flatMap(this.dtoMapper::projectMeshToDTO);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@PostMapping
	Mono<ProjectMeshDTO> createProjectMesh(@RequestBody ProjectMeshDTO dto) {
		return Mono.just(new ProjectMesh())
				.flatMap(p -> this.dtoMapper.updateProjectMeshFromDTO(p, dto))
				.flatMap(this.meshRepository::add)
				.flatMap(this.dtoMapper::projectMeshToDTO);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@GetMapping("/{id}")
	Mono<ProjectMeshDTO> getProjectMeshById(@PathVariable UUID id) {
		return this.meshRepository.getById(id)
				.flatMap(this.dtoMapper::projectMeshToDTO)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "ProjectMesh with id " + id + " not found")));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@PostMapping("/{id}")
	Mono<ProjectMeshDTO> updateProjectMesh(@PathVariable UUID id, @RequestBody ProjectMeshDTO dto) {
		return this.meshRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "ProjectMesh with id " + id + " not found")))
				.flatMap(mesh -> this.dtoMapper.updateProjectMeshFromDTO(mesh, dto))
				.flatMap(this.meshRepository::add)
				.flatMap(this.dtoMapper::projectMeshToDTO);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@DeleteMapping("/{id}")
	Mono<Void> deleteProjectMesh(@PathVariable UUID id) {
		return this.meshRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "ProjectMesh with id " + id + " not found")))
				.flatMap(this.meshRepository::remove);
	}


	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@PostMapping("/{id}/deploy")
	Mono<ProjectMeshDTO> triggerDeployment(@PathVariable UUID id) {
		return this.meshRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "ProjectMesh with id " + id + " not found")))
				.flatMap(mesh -> kubernetesDeploymentManager.deploy(mesh))
				.flatMap(dtoMapper::projectMeshToDTO);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@PostMapping("/{id}/stop")
	Mono<Void> stopDeployment(@PathVariable UUID id) {
		return this.meshRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "ProjectMesh with id " + id + " not found")))
				.flatMap(kubernetesDeploymentManager::stopDeployment)
				.then();
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}/component/{componentId}/configuration")
	Mono<DeployableConfigurationDTO> showCalculatedConfigurationOfComponent(@PathVariable UUID id, @PathVariable UUID componentId) {
		return this.meshRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "ProjectMesh with id " + id + " not found")))
				.map(mesh -> mesh.getComponentById(componentId))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesh component with id " + componentId + " not found")))
				.map(this.configurationDTOMapper::create);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@PostMapping("/{id}/component/{componentId}/deploy")
	Mono<ProjectMeshDTO> triggerDeploymentOfComponent(@PathVariable UUID id, @PathVariable UUID componentId) {
		return this.meshRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "ProjectMesh with id " + id + " not found")))
				.map(mesh -> mesh.getComponentById(componentId))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesh component with id " + componentId + " not found")))
				.flatMap(component -> kubernetesDeploymentManager.deploy(component))
				.map(component -> component.getOwner())
				.flatMap(dtoMapper::projectMeshToDTO);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@PostMapping("/{id}/component/{componentId}/stop")
	Mono<ProjectMeshDTO> stopDeploymentOfComponent(@PathVariable UUID id, @PathVariable UUID componentId) {
		return this.meshRepository.getById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "ProjectMesh with id " + id + " not found")))
				.map(mesh -> mesh.getComponentById(componentId))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Mesh component with id " + componentId + " not found")))
				.flatMap(kubernetesDeploymentManager::stopDeployment)
				.flatMap(dtoMapper::projectMeshToDTO);
	}
}
