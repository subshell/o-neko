package io.oneko.project.rest;

import io.oneko.configuration.Controllers;
import io.oneko.docker.DockerRegistry;
import io.oneko.docker.DockerRegistryRepository;
import io.oneko.docker.ReadableDockerRegistry;
import io.oneko.kubernetes.DeploymentManager;
import io.oneko.project.Project;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ReadableProject;
import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.WritableProject;
import io.oneko.project.WritableProjectVersion;
import io.oneko.project.rest.export.ProjectExportDTO;
import io.oneko.project.rest.export.ProjectExportDTOMapper;
import io.oneko.search.ProjectSearchResultEntry;
import io.oneko.search.SearchResult;
import io.oneko.search.SearchService;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
@RequestMapping(ProjectController.PATH)
public class ProjectController {

	public static final String PATH = Controllers.ROOT_PATH + "/project";

	private final ProjectRepository projectRepository;
	private final DockerRegistryRepository dockerRegistryRepository;
	private final ProjectDTOMapper dtoMapper;
	private final DeployableConfigurationDTOMapper configurationDTOMapper;
	private final DeploymentManager deploymentManager;
	private final SearchService searchService;

	public ProjectController(ProjectRepository projectRepository, DockerRegistryRepository dockerRegistryRepository,
			ProjectDTOMapper dtoMapper,
			DeployableConfigurationDTOMapper configurationDTOMapper,
			DeploymentManager deploymentManager,
			SearchService searchService) {
		this.projectRepository = projectRepository;
		this.dockerRegistryRepository = dockerRegistryRepository;
		this.dtoMapper = dtoMapper;
		this.configurationDTOMapper = configurationDTOMapper;
		this.deploymentManager = deploymentManager;
		this.searchService = searchService;
	}

	/**
	 * Ensures that the DTO refers to a docker registry that actually exists...
	 */
	private UUID getDockerRegistryIdForProject(Project project, ProjectDTO dto) {
		//TODO: that check might not belong here...
		if (Objects.equals(project.getDockerRegistryId(), dto.getDockerRegistryUUID())) {
			//no change, so...
			return project.getDockerRegistryId();
		} else if (Objects.isNull(dto.getDockerRegistryUUID())) {
			return null;
		} else {
			return dockerRegistryRepository.getById(dto.getDockerRegistryUUID())
					.map(DockerRegistry::getUuid)
					.orElseThrow(
							() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DockerRegistry with id " + dto.getDockerRegistryUUID() + " not found"));
		}
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@GetMapping
	List<ProjectDTO> getAllProjects() {
		return this.projectRepository.getAll()
				.stream()
				.map(this.dtoMapper::projectToDTO)
				.collect(Collectors.toList());
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@PostMapping
	ProjectDTO createProject(@RequestBody ProjectDTO dto) {
		ReadableDockerRegistry dockerRegistry = dockerRegistryRepository.getById(dto.getDockerRegistryUUID())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DockerRegistry with id " + dto.getDockerRegistryUUID() + " not found"));
		WritableProject newProject = new WritableProject(dockerRegistry.getId());
		dtoMapper.updateProjectFromDTO(newProject, dto, dockerRegistry.getId());
		ReadableProject persistedProject = projectRepository.add(newProject);
		return dtoMapper.projectToDTO(persistedProject);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@GetMapping("/{id}")
	ProjectDTO getProjectById(@PathVariable UUID id) {
		return this.projectRepository.getById(id)
				.map(this.dtoMapper::projectToDTO)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id " + id + " not found"));
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@PostMapping("/{id}")
	ProjectDTO updateProject(@PathVariable UUID id, @RequestBody ProjectDTO dto) {
		WritableProject project = getProjectOr404(id).writable();
		final UUID dockerRegistryId = getDockerRegistryIdForProject(project, dto);
		dtoMapper.updateProjectFromDTO(project, dto, dockerRegistryId);
		final ReadableProject persisted = projectRepository.add(project);
		return dtoMapper.projectToDTO(persisted);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')") // this method exists to let VIEWERs change values of variables
	@PostMapping("/{id}/version/{versionId}/templateVariables")
	ProjectDTO updateProjectVersionTemplateVariables(@PathVariable UUID id, @PathVariable UUID versionId, @RequestBody ProjectDTO dto) {
		WritableProject project = getProjectOr404(id).writable();
		project.getVersionById(versionId)
				.ifPresent(version -> dto.getVersions()
						.stream()
						.filter(v -> v.getUuid()
								.equals(version.getId()))
						.findFirst()
						.ifPresent(versionFromDto -> version.setTemplateVariables(versionFromDto.getTemplateVariables())));
		final ReadableProject persisted = projectRepository.add(project);
		return dtoMapper.projectToDTO(persisted);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@DeleteMapping("/{id}")
	void deleteProject(@PathVariable UUID id) {
		final ReadableProject project = getProjectOr404(id);
		projectRepository.remove(project);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@GetMapping("/{id}/export")
	ProjectExportDTO exportProject(@PathVariable UUID id) {
		final ReadableProject project = getProjectOr404(id);
		final ProjectDTO projectDTO = dtoMapper.projectToDTO(project);
		return ProjectExportDTOMapper.MAPPER.toProjectExportDto(projectDTO);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@PostMapping("/{id}/version/{versionId}/deploy")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void triggerDeploymentOfVersion(@PathVariable UUID id, @PathVariable UUID versionId) {
		WritableProject project = getProjectOr404(id).writable();
		WritableProjectVersion projectVersion = project.getVersionById(versionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project version with id " + versionId + " not found"));
		deploymentManager.deployAsync(projectVersion);
	}


	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@PostMapping("/{id}/version/{versionId}/stop")
	void stopDeployment(@PathVariable UUID id, @PathVariable UUID versionId) {
		WritableProject project = getProjectOr404(id).writable();
		WritableProjectVersion projectVersion = project.getVersionById(versionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project version with id " + versionId + " not found"));
		deploymentManager.stopDeploymentAsync(projectVersion);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER')")
	@GetMapping("/{id}/version/{versionId}/configuration")
	DeployableConfigurationDTO showCalculatedConfigurationOfVersion(@PathVariable UUID id, @PathVariable UUID versionId) {
		ReadableProject project = getProjectOr404(id);
		ReadableProjectVersion projectVersion = project.getVersionById(versionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project version with id " + versionId + " not found"));
		return configurationDTOMapper.create(projectVersion);
	}

	@GetMapping("/search")
	SearchResult findProjectsOrVersions(@RequestParam String query) {
		return searchService.findProjectsAndVersions(query);
	}

	private ReadableProject getProjectOr404(UUID id) {
		return projectRepository.getById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project with id " + id + " not found"));
	}

}
