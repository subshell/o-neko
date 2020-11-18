package io.oneko.project.rest;

import com.google.common.base.MoreObjects;
import io.oneko.automations.LifetimeBehaviourDTOMapper;
import io.oneko.deployable.AggregatedDeploymentStatus;
import io.oneko.kubernetes.deployments.*;
import io.oneko.namespace.ImplicitNamespace;
import io.oneko.namespace.rest.NamespaceDTOMapper;
import io.oneko.project.*;
import io.oneko.templates.rest.ConfigurationTemplateDTOMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
public class ProjectDTOMapper {
	private static final List<String> IMPLICIT_PROJECT_TEMPLATE_VARIABLES = Arrays.asList(
			ProjectConstants.TemplateVariablesNames.PROJECT_NAME,
			ProjectConstants.TemplateVariablesNames.ONEKO_PROJECT
	);

	private static final List<String> IMPLICIT_PROJECT_VERSION_TEMPLATE_VARIABLES = Arrays.asList(
			ProjectConstants.TemplateVariablesNames.VERSION_NAME,
			ProjectConstants.TemplateVariablesNames.SAFE_VERSION_NAME,
			ProjectConstants.TemplateVariablesNames.ONEKO_VERSION
	);

	private final ConfigurationTemplateDTOMapper templateDTOMapper;
	private final NamespaceDTOMapper namespaceDTOMapper;
	private final DeploymentRepository deploymentRepository;
	private final LifetimeBehaviourDTOMapper lifetimeBehaviourDTOMapper;

	public ProjectDTOMapper(ConfigurationTemplateDTOMapper templateDTOMapper, NamespaceDTOMapper namespaceDTOMapper, DeploymentRepository deploymentRepository, LifetimeBehaviourDTOMapper lifetimeBehaviourDTOMapper) {
		this.templateDTOMapper = templateDTOMapper;
		this.namespaceDTOMapper = namespaceDTOMapper;
		this.deploymentRepository = deploymentRepository;
		this.lifetimeBehaviourDTOMapper = lifetimeBehaviourDTOMapper;
	}

	public ProjectDTO projectToDTO(ReadableProject project) {
		ProjectDTO dto = new ProjectDTO();
		dto.setUuid(project.getId());
		dto.setName(project.getName());
		dto.setDockerRegistryUUID(project.getDockerRegistryId());
		dto.setImageName(project.getImageName());
		dto.setNewVersionsDeploymentBehaviour(project.getNewVersionsDeploymentBehaviour());
		dto.setDefaultLifetimeBehaviour(project.getDefaultLifetimeBehaviour().map(lifetimeBehaviourDTOMapper::toLifetimeBehaviourDTO).orElse(null));
		dto.setDefaultConfigurationTemplates(project.getDefaultConfigurationTemplates().stream()
				.map(templateDTOMapper::toDTO)
				.collect(Collectors.toList()));
		dto.setTemplateVariables(toTemplateVariableDTOs(project.getTemplateVariables()));

		final List<ProjectVersionDTO> versionDTOs = projectVersionsToDTO(project.getVersions());
		dto.setVersions(versionDTOs);
		dto.setStatus(aggregateDeploymentStatus(versionDTOs));
		return dto;
	}

	private List<TemplateVariableDTO> toTemplateVariableDTOs(List<ReadableTemplateVariable> templateVariables) {
		return templateVariables.stream()
				.map(this::toTemplateVariableDTO)
				.collect(Collectors.toList());
	}

	private TemplateVariableDTO toTemplateVariableDTO(ReadableTemplateVariable templateVariable) {
		return TemplateVariableDTO.builder()
				.id(templateVariable.getId())
				.name(templateVariable.getName())
				.label(templateVariable.getLabel())
				.values(templateVariable.getValues())
				.useValues(templateVariable.isUseValues())
				.defaultValue(templateVariable.getDefaultValue())
				.showOnDashboard(templateVariable.isShowOnDashboard())
				.build();
	}

	private List<WritableTemplateVariable> fromTemplateVariableDTOs(List<TemplateVariableDTO> templateVariables) {
		return templateVariables.stream()
				.map(this::fromTemplateVariableDTO)
				.collect(Collectors.toList());
	}

	private WritableTemplateVariable fromTemplateVariableDTO(TemplateVariableDTO templateVariable) {
		return new WritableTemplateVariable(MoreObjects.firstNonNull(templateVariable.getId(), UUID.randomUUID()),
				templateVariable.getName(),
				templateVariable.getLabel(),
				templateVariable.getValues(),
				templateVariable.isUseValues(),
				templateVariable.getDefaultValue(),
				templateVariable.isShowOnDashboard());
	}

	private AggregatedDeploymentStatus aggregateDeploymentStatus(Collection<ProjectVersionDTO> versionDTOs) {
		return DeploymentDTOs.aggregate(versionDTOs.stream()
				.map(ProjectVersionDTO::getDeployment)
				.collect(Collectors.toList()));
	}

	private List<ProjectVersionDTO> projectVersionsToDTO(Collection<ReadableProjectVersion> versions) {
		return versions.stream()
				.map(this::projectVersionToDTO)
				.collect(Collectors.toList());
	}

	private ProjectVersionDTO projectVersionToDTO(ReadableProjectVersion version) {
		final Deployment deployment = deploymentRepository.findByDeployableId(version.getId()).orElse(null);
		return projectVersionToDTO(version, deployment);
	}

	private ProjectVersionDTO projectVersionToDTO(ReadableProjectVersion version, Deployment deployment) {
		ProjectVersionDTO dto = new ProjectVersionDTO();
		dto.setUuid(version.getUuid());
		dto.setName(version.getName());
		dto.setDeploymentBehaviour(version.getDeploymentBehaviour());
		dto.setAvailableTemplateVariables(toTemplateVariableDTOs(version.getProject().getTemplateVariables()));
		dto.setTemplateVariables(version.getTemplateVariables());
		dto.setDeployment(DeploymentDTO.create(version.getDeploymentBehaviour(), version.getId(), deployment));
		dto.setUrls(version.getUrls());
		dto.setOutdated(version.isOutdated());
		dto.setConfigurationTemplates(version.getConfigurationTemplates().stream()
				.map(templateDTOMapper::toDTO)
				.collect(Collectors.toList()));
		dto.setLifetimeBehaviour(version.getLifetimeBehaviour().map(lifetimeBehaviourDTOMapper::toLifetimeBehaviourDTO).orElse(null));
		dto.setNamespace(namespaceDTOMapper.namespaceToDTO(version.getNamespace()));
		dto.setImplicitNamespace(namespaceDTOMapper.namespaceToDTO(new ImplicitNamespace(version)));
		dto.setDesiredState(version.getDesiredState());
		dto.setImageUpdatedDate(version.getImageUpdatedDate());
		return dto;
	}

	public void updateProjectFromDTO(WritableProject project, ProjectDTO projectDTO, UUID registryId) {
		//id can not be changed
		project.setName(projectDTO.getName());
		project.setImageName(projectDTO.getImageName());
		project.assignToNewRegistry(registryId);
		ofNullable(projectDTO.getNewVersionsDeploymentBehaviour()).ifPresent(project::setNewVersionsDeploymentBehaviour);
		project.setDefaultConfigurationTemplates(templateDTOMapper.updateFromDTOs(project.getDefaultConfigurationTemplates(), projectDTO.getDefaultConfigurationTemplates()));
		project.setDefaultLifetimeBehaviour(ofNullable(projectDTO.getDefaultLifetimeBehaviour()).map(lifetimeBehaviourDTOMapper::toLifetimeBehaviour).orElse(null));
		project.setTemplateVariables(fromTemplateVariableDTOs(projectDTO.getTemplateVariables()));

		updateProjectVersionsFromDTO(project.getVersions(), projectDTO.getVersions());
	}

	private void updateProjectVersionsFromDTO(Collection<WritableProjectVersion> versions, List<ProjectVersionDTO> versionDTOs) {
		final Map<UUID, ProjectVersionDTO> versionDTOsById = versionDTOs.stream().collect(Collectors.toMap(ProjectVersionDTO::getUuid, Function.identity()));
		versions.forEach(version -> updateProjectVersionFromDTO(version, versionDTOsById.get(version.getUuid())));
	}

	private void updateProjectVersionFromDTO(WritableProjectVersion version, ProjectVersionDTO projectVersionDTO) {
		if (projectVersionDTO == null) {
			return;
		}
		ofNullable(projectVersionDTO.getDeploymentBehaviour()).ifPresent(version::setDeploymentBehaviour);

		final Map<String, String> templateVariables = new HashMap<>();
		for (Map.Entry<String, String> entry : projectVersionDTO.getTemplateVariables().entrySet()) {
			if (ListUtils.union(IMPLICIT_PROJECT_TEMPLATE_VARIABLES, IMPLICIT_PROJECT_VERSION_TEMPLATE_VARIABLES).contains(entry.getKey())) {
				continue;
			}
			templateVariables.put(entry.getKey(), entry.getValue());
		}
		version.setTemplateVariables(templateVariables);

		version.setConfigurationTemplates(templateDTOMapper.updateFromDTOs(version.getConfigurationTemplates(), projectVersionDTO.getConfigurationTemplates()));
		version.setLifetimeBehaviour(ofNullable(projectVersionDTO.getLifetimeBehaviour()).filter(dto -> dto.getDaysToLive() != -1).map(lifetimeBehaviourDTOMapper::toLifetimeBehaviour).orElse(null));
		namespaceDTOMapper.updateNamespaceOfOwner(version, projectVersionDTO.getNamespace());
		updateDeploymentStatusOfVersion(version);
	}

	private void updateDeploymentStatusOfVersion(WritableProjectVersion version) {
		this.deploymentRepository.findByDeployableId(version.getId()).ifPresent(deployment -> {
			if (shouldVersionBeMarkedAsOutdated(version, deployment)) {
				version.setOutdated(true);
			}
		});
	}

	private boolean shouldVersionBeMarkedAsOutdated(WritableProjectVersion version, Deployment deployment) {
		if (deployment == null || deployment.getStatus() == DeployableStatus.NotScheduled) {
			return false;
		}
		if (CollectionUtils.containsAny(version.getProject().getDirtyProperties(), Arrays.asList("defaultConfigurationTemplates", "imageName", "defaultTemplateVariables", "dockerRegistry"))) {
			return true;
		}
		return CollectionUtils.containsAny(version.getDirtyProperties(), Arrays.asList("configurationTemplates", "templateVariables"));
	}
}
