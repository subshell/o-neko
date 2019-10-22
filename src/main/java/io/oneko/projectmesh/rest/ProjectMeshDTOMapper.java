package io.oneko.projectmesh.rest;

import static java.util.Optional.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.oneko.automations.LifetimeBehaviourDTOMapper;
import io.oneko.deployable.AggregatedDeploymentStatus;
import io.oneko.kubernetes.deployments.Deployment;
import io.oneko.kubernetes.deployments.DeploymentDTO;
import io.oneko.kubernetes.deployments.DeploymentDTOs;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import io.oneko.namespace.ImplicitNamespace;
import io.oneko.namespace.rest.NamespaceDTOMapper;
import io.oneko.project.Project;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.projectmesh.MeshComponent;
import io.oneko.projectmesh.ProjectMesh;
import io.oneko.templates.rest.ConfigurationTemplateDTOMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProjectMeshDTOMapper {
	private final NamespaceDTOMapper namespaceDTOMapper;
	private final ConfigurationTemplateDTOMapper templateDTOMapper;
	private final LifetimeBehaviourDTOMapper lifetimeBehaviourDTOMapper;
	private final DeploymentRepository deploymentRepository;
	private final ProjectRepository projectRepository;

	public ProjectMeshDTOMapper(NamespaceDTOMapper namespaceDTOMapper, ConfigurationTemplateDTOMapper templateDTOMapper, LifetimeBehaviourDTOMapper lifetimeBehaviourDTOMapper, DeploymentRepository deploymentRepository, ProjectRepository projectRepository) {
		this.namespaceDTOMapper = namespaceDTOMapper;
		this.templateDTOMapper = templateDTOMapper;
		this.lifetimeBehaviourDTOMapper = lifetimeBehaviourDTOMapper;
		this.deploymentRepository = deploymentRepository;
		this.projectRepository = projectRepository;
	}

	public Mono<ProjectMeshDTO> projectMeshToDTO(ProjectMesh mesh) {
		ProjectMeshDTO dto = new ProjectMeshDTO();
		dto.setId(mesh.getId());
		dto.setName(mesh.getName());
		dto.setNamespace(namespaceDTOMapper.namespaceToDTO(mesh.getNamespace()));
		dto.setImplicitNamespace(namespaceDTOMapper.namespaceToDTO(new ImplicitNamespace(mesh)));
		dto.setDeploymentBehaviour(mesh.getDeploymentBehaviour());
		dto.setLifetimeBehaviour(mesh.getLifetimeBehaviour().map(lifetimeBehaviourDTOMapper::toLifetimeBehaviourDTO).orElse(null));
		return componentsToDTO(mesh.getComponents())
				.collectList()
				.map(components -> {
					dto.setComponents(components);
					final AggregatedDeploymentStatus aggregatedDeploymentStatus = aggregateDeploymentStatus(components);
					dto.setStatus(aggregatedDeploymentStatus);
					return dto;
				});
	}

	private AggregatedDeploymentStatus aggregateDeploymentStatus(Collection<MeshComponentDTO> componentDTOs) {
		return DeploymentDTOs.aggregate(componentDTOs.stream()
				.map(MeshComponentDTO::getDeployment)
				.collect(Collectors.toList()));
	}

	private Flux<MeshComponentDTO> componentsToDTO(Collection<MeshComponent> components) {
		return Flux.concat(components.stream()
				.map(this::componentToDTO)
				.collect(Collectors.toList()));
	}

	private Mono<MeshComponentDTO> componentToDTO(MeshComponent component) {
		return deploymentRepository.findByDeployableId(component.getId())
				.map(deployment -> componentToDTO(component, deployment))
				.switchIfEmpty(Mono.justOrEmpty(componentToDTO(component, null)));
	}

	private MeshComponentDTO componentToDTO(MeshComponent component, Deployment deployment) {
		MeshComponentDTO dto = new MeshComponentDTO();
		dto.setId(component.getId());
		dto.setName(component.getName());
		dto.setProjectId(component.getProject().getId());
		dto.setProjectVersionId(component.getProjectVersion().getId());
		dto.setTemplateVariables(component.getTemplateVariables());
		dto.setConfigurationTemplates(component.getConfigurationTemplates().stream()
				.map(templateDTOMapper::toDTO)
				.collect(Collectors.toList()));
		dto.setOutdated(component.isOutdated());
		dto.setUrls(component.getUrls());
		dto.setDeployment(DeploymentDTO.create(component.getOwner().getDeploymentBehaviour(), component.getId(), deployment));
		dto.setDesiredState(component.getDesiredState());
		return dto;
	}

	public Mono<ProjectMesh> updateProjectMeshFromDTO(ProjectMesh mesh, ProjectMeshDTO dto) {
		//id can not be changed
		mesh.setName(dto.getName());
		mesh.setDeploymentBehaviour(dto.getDeploymentBehaviour());
		mesh.setLifetimeBehaviour(ofNullable(dto.getLifetimeBehaviour()).map(lifetimeBehaviourDTOMapper::toLifetimeBehaviour).orElse(null));
		return updateComponentsFromDTO(mesh, mesh.getComponents(), dto.getComponents())
				.collectList()
				.then(namespaceDTOMapper.updateNamespaceOfOwner(mesh, dto.getNamespace()));
	}

	private Flux<MeshComponent> updateComponentsFromDTO(ProjectMesh mesh, Collection<MeshComponent> components, List<MeshComponentDTO> componentDTOs) {
		final Map<UUID, MeshComponentDTO> componentDTOsById = new HashMap<>();
		final List<MeshComponentDTO> newComponentDTOs = new ArrayList<>();
		for (MeshComponentDTO componentDTO : componentDTOs) {
			if (componentDTO.getId() != null) {
				componentDTOsById.put(componentDTO.getId(), componentDTO);
			} else {
				newComponentDTOs.add(componentDTO);
			}
		}
		final List<Mono<MeshComponent>> updatedExistingComponents = components.stream()
				.map(component -> updateComponentFromDTO(component, componentDTOsById.get(component.getId())).thenReturn(component))
				.collect(Collectors.toList());
		final List<Mono<MeshComponent>> newComponents = newComponentDTOs.stream()
				.map(dto -> createComponentFromDTO(mesh, dto))
				.collect(Collectors.toList());
		return Flux.concat(updatedExistingComponents)
				.concatWith(Flux.concat(newComponents));
	}

	private Mono<MeshComponent> createComponentFromDTO(ProjectMesh owner, MeshComponentDTO dto) {
		return this.projectRepository.getById(dto.getProjectId())
				.flatMap(p -> createComponentFromDTO(owner, dto, p))
				.filter(Objects::nonNull);
	}

	private Mono<MeshComponent> createComponentFromDTO(ProjectMesh owner, MeshComponentDTO dto, Project project) {
		final Optional<ProjectVersion> versionByUUID = project.getVersionByUUID(dto.getProjectVersionId());
		if (versionByUUID.isEmpty()) {
			return Mono.empty();
		}
		final MeshComponent component = owner.createComponent(dto.getName(), project, versionByUUID.get());
		return updateComponentFromDTO(component, dto);
	}

	private Mono<MeshComponent> updateComponentFromDTO(MeshComponent component, MeshComponentDTO componentDTO) {
		if (componentDTO == null) {
			component.getOwner().removeComponent(component.getName());
			return Mono.empty();
		}
		component.setName(componentDTO.getName());
		component.setTemplateVariables(componentDTO.getTemplateVariables());
		component.setConfigurationTemplates(templateDTOMapper.updateFromDTOs(component.getConfigurationTemplates(), componentDTO.getConfigurationTemplates()));
		if (!Objects.equals(component.getProjectVersion().getId(), componentDTO.getProjectVersionId())) {
			final Optional<ProjectVersion> versionByUUID = component.getProject().getVersionByUUID(componentDTO.getProjectVersionId());
			versionByUUID.ifPresent(component::setProjectVersion);
		}
		return Mono.just(component);
	}

}
