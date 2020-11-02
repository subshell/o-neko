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
import io.oneko.project.ReadableProject;
import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.ProjectRepository;
import io.oneko.projectmesh.ReadableMeshComponent;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableMeshComponent;
import io.oneko.projectmesh.WritableProjectMesh;
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

	public Mono<ProjectMeshDTO> projectMeshToDTO(ReadableProjectMesh mesh) {
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

	private Flux<MeshComponentDTO> componentsToDTO(Collection<ReadableMeshComponent> components) {
		return Flux.concat(components.stream()
				.map(this::componentToDTO)
				.collect(Collectors.toList()));
	}

	private Mono<MeshComponentDTO> componentToDTO(ReadableMeshComponent component) {
		return deploymentRepository.findByDeployableId(component.getId())
				.map(deployment -> componentToDTO(component, deployment))
				.switchIfEmpty(Mono.justOrEmpty(componentToDTO(component, null)));
	}

	private MeshComponentDTO componentToDTO(ReadableMeshComponent component, Deployment deployment) {
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

	public Mono<WritableProjectMesh> updateProjectMeshFromDTO(WritableProjectMesh mesh, ProjectMeshDTO dto) {
		//id can not be changed
		mesh.setName(dto.getName());
		mesh.setDeploymentBehaviour(dto.getDeploymentBehaviour());
		mesh.setLifetimeBehaviour(ofNullable(dto.getLifetimeBehaviour()).map(lifetimeBehaviourDTOMapper::toLifetimeBehaviour).orElse(null));
		return updateComponentsFromDTO(mesh, mesh.getComponents(), dto.getComponents())
				.collectList()
				.then(namespaceDTOMapper.updateNamespaceOfOwner(mesh, dto.getNamespace()));
	}

	private Flux<WritableMeshComponent> updateComponentsFromDTO(WritableProjectMesh mesh, Collection<WritableMeshComponent> components, List<MeshComponentDTO> componentDTOs) {
		final Map<UUID, MeshComponentDTO> componentDTOsById = new HashMap<>();
		final List<MeshComponentDTO> newComponentDTOs = new ArrayList<>();
		for (MeshComponentDTO componentDTO : componentDTOs) {
			if (componentDTO.getId() != null) {
				componentDTOsById.put(componentDTO.getId(), componentDTO);
			} else {
				newComponentDTOs.add(componentDTO);
			}
		}
		final List<Mono<WritableMeshComponent>> updatedExistingComponents = components.stream()
				.map(component -> updateComponentFromDTO(component, componentDTOsById.get(component.getId())).thenReturn(component))
				.collect(Collectors.toList());
		final List<Mono<WritableMeshComponent>> newComponents = newComponentDTOs.stream()
				.map(dto -> createComponentFromDTO(mesh, dto))
				.collect(Collectors.toList());
		return Flux.concat(updatedExistingComponents)
				.concatWith(Flux.concat(newComponents));
	}

	private Mono<WritableMeshComponent> createComponentFromDTO(WritableProjectMesh owner, MeshComponentDTO dto) {
		return this.projectRepository.getById(dto.getProjectId())
				.flatMap(p -> createComponentFromDTO(owner, dto, p))
				.filter(Objects::nonNull);
	}

	private Mono<WritableMeshComponent> createComponentFromDTO(WritableProjectMesh owner, MeshComponentDTO dto, ReadableProject project) {
		final Optional<ReadableProjectVersion> versionByUUID = project.getVersionById(dto.getProjectVersionId());
		if (versionByUUID.isEmpty()) {
			return Mono.empty();
		}
		final WritableMeshComponent component = owner.createComponent(dto.getName(), project, versionByUUID.get());
		return updateComponentFromDTO(component, dto);
	}

	private Mono<WritableMeshComponent> updateComponentFromDTO(WritableMeshComponent component, MeshComponentDTO componentDTO) {
		if (componentDTO == null) {
			component.getOwner().removeComponent(component.getName());
			return Mono.empty();
		}
		component.setName(componentDTO.getName());
		component.setTemplateVariables(componentDTO.getTemplateVariables());
		component.setConfigurationTemplates(templateDTOMapper.updateFromDTOs(component.getConfigurationTemplates(), componentDTO.getConfigurationTemplates()));
		if (!Objects.equals(component.getProjectVersion().getId(), componentDTO.getProjectVersionId())) {
			final Optional<ReadableProjectVersion> versionByUUID = component.getProject().getVersionById(componentDTO.getProjectVersionId());
			versionByUUID.ifPresent(component::setProjectVersion);
		}
		return Mono.just(component);
	}

}
