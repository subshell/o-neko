package io.oneko.projectmesh.rest;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
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
import io.oneko.kubernetes.KubernetesDeploymentManager;
import io.oneko.project.rest.DeployableConfigurationDTO;
import io.oneko.project.rest.DeployableConfigurationDTOMapper;
import io.oneko.projectmesh.ProjectMeshRepository;
import io.oneko.projectmesh.ReadableMeshComponent;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableProjectMesh;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(ProjectMeshController.PATH)
public class ProjectMeshController {

	public static final String PATH = Controllers.ROOT_PATH + "/projectMesh";
	private static final Function<UUID, Supplier<ResponseStatusException>> MESH_NOT_FOUND_EXCEPTION_FN = (UUID id) -> () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("ProjectMesh with id %s not found", id));

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
	public List<ProjectMeshDTO> getAllProjectMeshes() {
		return this.meshRepository.getAll().stream()
				.map(this.dtoMapper::projectMeshToDTO)
				.collect(Collectors.toList());
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@PostMapping
	public ProjectMeshDTO createProjectMesh(@RequestBody ProjectMeshDTO dto) {
		final var mesh = new WritableProjectMesh();
		dtoMapper.updateProjectMeshFromDTO(mesh, dto);
		final var readableMesh = meshRepository.add(mesh);
		return dtoMapper.projectMeshToDTO(readableMesh);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@GetMapping("/{id}")
	public ProjectMeshDTO getProjectMeshById(@PathVariable UUID id) {
		return this.meshRepository.getById(id)
				.map(this.dtoMapper::projectMeshToDTO)
				.orElseThrow(MESH_NOT_FOUND_EXCEPTION_FN.apply(id));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@PostMapping("/{id}")
	public ProjectMeshDTO updateProjectMesh(@PathVariable UUID id, @RequestBody ProjectMeshDTO dto) {
		return this.meshRepository.getById(id)
				.map(ReadableProjectMesh::writable)
				.map(mesh -> this.dtoMapper.updateProjectMeshFromDTO(mesh, dto))
				.map(this.meshRepository::add)
				.map(this.dtoMapper::projectMeshToDTO)
				.orElseThrow(MESH_NOT_FOUND_EXCEPTION_FN.apply(id));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@DeleteMapping("/{id}")
	public void deleteProjectMesh(@PathVariable UUID id) {
		this.meshRepository.getById(id)
				.ifPresentOrElse(this.meshRepository::remove, () -> MESH_NOT_FOUND_EXCEPTION_FN.apply(id).get());
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@PostMapping("/{id}/deploy")
	public ProjectMeshDTO triggerDeployment(@PathVariable UUID id) {
		return this.meshRepository.getById(id)
				.map(ReadableProjectMesh::writable)
				.map(kubernetesDeploymentManager::deploy)
				.map(dtoMapper::projectMeshToDTO)
				.orElseThrow(MESH_NOT_FOUND_EXCEPTION_FN.apply(id));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@PostMapping("/{id}/stop")
	public void stopDeployment(@PathVariable UUID id) {
		this.meshRepository.getById(id)
				.map(ReadableProjectMesh::writable)
				.ifPresentOrElse(kubernetesDeploymentManager::stopDeployment, () -> MESH_NOT_FOUND_EXCEPTION_FN.apply(id).get());
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}/component/{componentId}/configuration")
	public DeployableConfigurationDTO showCalculatedConfigurationOfComponent(@PathVariable UUID id, @PathVariable UUID componentId) {
		return configurationDTOMapper.create(getMeshComponentOrThrow(id, componentId));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@PostMapping("/{id}/component/{componentId}/deploy")
	public ProjectMeshDTO triggerDeploymentOfComponent(@PathVariable UUID id, @PathVariable UUID componentId) {
		final var component = getMeshComponentOrThrow(id, componentId);
		final var deployedComponent = kubernetesDeploymentManager.deploy(component.writable());

		return dtoMapper.projectMeshToDTO(deployedComponent.getOwner());
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@PostMapping("/{id}/component/{componentId}/stop")
	public ProjectMeshDTO stopDeploymentOfComponent(@PathVariable UUID id, @PathVariable UUID componentId) {
		final var component = getMeshComponentOrThrow(id, componentId);
		final var deployedComponent = kubernetesDeploymentManager.stopDeployment(component.writable());

		return dtoMapper.projectMeshToDTO(deployedComponent);
	}

	private ReadableMeshComponent getMeshComponentOrThrow(UUID id, UUID componentId) {
		final var mesh = this.meshRepository.getById(id).orElseThrow(MESH_NOT_FOUND_EXCEPTION_FN.apply(id));

		return mesh.getComponentById(componentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Mesh component with id %s not found", componentId)));
	}
}
