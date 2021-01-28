package io.oneko.project.persistence;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.namespace.NamespaceRepository;
import io.oneko.namespace.ReadableNamespace;
import io.oneko.project.*;
import io.oneko.project.event.EventAwareProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Profile(Profiles.MONGO)
class ProjectMongoRepository extends EventAwareProjectRepository {

	private final ProjectMongoSpringRepository innerProjectRepo;
	private final NamespaceRepository namespaceRepository;

	@Autowired
	ProjectMongoRepository(ProjectMongoSpringRepository innerProjectRepo, NamespaceRepository namespaceRepository, EventDispatcher eventDispatcher) {
		super(eventDispatcher);
		this.innerProjectRepo = innerProjectRepo;
		this.namespaceRepository = namespaceRepository;
	}

	@Override
	public Optional<ReadableProject> getById(UUID projectId) {
		return this.innerProjectRepo.findById(projectId).map(this::fromProjectMongo);
	}

	@Override
	public Optional<ReadableProject> getByName(String name) {
		return this.innerProjectRepo.findByName(name).map(this::fromProjectMongo);
	}

	@Override
	public List<ReadableProject> getByDockerRegistryUuid(UUID dockerRegistryUUID) {
		return this.innerProjectRepo.findByDockerRegistryUUID(dockerRegistryUUID).stream().map(this::fromProjectMongo).collect(Collectors.toList());
	}

	@Override
	public List<ReadableProject> getByHelmRegistryId(UUID helmRegistryId) {
		return this.innerProjectRepo.findAll().stream()
				.filter(project ->
						// does the base project reference this helm registry?
						project.getDefaultConfigurationTemplates().stream().anyMatch(template -> templateReferencesHelmRegistry(template, helmRegistryId)) ||
						// does any of the versions reference this helm registry?
						project.getVersions().stream().flatMap(version -> version.getConfigurationTemplates().stream()).anyMatch(template -> templateReferencesHelmRegistry(template, helmRegistryId)))
				.map(this::fromProjectMongo).collect(Collectors.toList());
	}


	@Override
	public List<ReadableProject> getAll() {
		return this.innerProjectRepo.findAll().stream().map(this::fromProjectMongo).collect(Collectors.toList());
	}

	@Override
	protected ReadableProject addInternally(WritableProject project) {
		ProjectMongo projectMongo = this.toProjectMongo(project);
		return this.fromProjectMongo(this.innerProjectRepo.save(projectMongo));
	}

	@Override
	protected void removeInternally(Project<?, ?> project) {
		this.innerProjectRepo.deleteById(project.getId());
	}

	private boolean templateReferencesHelmRegistry(ConfigurationTemplateMongo configurationTemplate, UUID helmRegistryId) {
		return helmRegistryId.equals(configurationTemplate.getHelmRegistryId());
	}

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Mapping stuff
     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

	private ProjectMongo toProjectMongo(WritableProject project) {
		ProjectMongo projectMongo = new ProjectMongo();
		projectMongo.setProjectUuid(project.getId());
		projectMongo.setName(project.getName());
		projectMongo.setImageName(project.getImageName());
		projectMongo.setNewVersionsDeploymentBehaviour(project.getNewVersionsDeploymentBehaviour());
		projectMongo.setDefaultConfigurationTemplates(ConfigurationTemplateMongoMapper.toConfigurationTemplateMongos(project.getDefaultConfigurationTemplates()));
		projectMongo.setTemplateVariables(this.toTemplateVariablesMongo(project.getTemplateVariables()));
		projectMongo.setNamespace(project.getNamespace());

		if (project.isOrphan()) {
			projectMongo.setDockerRegistryUUID(null);
		} else {
			projectMongo.setDockerRegistryUUID(project.getDockerRegistryId());
		}

		List<ProjectVersionMongo> versionsMongo = project.getVersions().stream()
				.map(this::toProjectVersionMongo)
				.collect(Collectors.toList());
		projectMongo.setVersions(versionsMongo);
		projectMongo.setDefaultLifetimeBehaviour(project.getDefaultLifetimeBehaviour().orElse(null));

		return projectMongo;
	}

	private List<ReadableTemplateVariable> fromTemplateVariablesMongo(List<TemplateVariableMongo> templateVariables) {
		return templateVariables.stream()
				.map(this::fromTemplateVariableMongo)
				.collect(Collectors.toList());
	}

	private List<TemplateVariableMongo> toTemplateVariablesMongo(List<WritableTemplateVariable> templateVariables) {
		return templateVariables.stream()
				.map(this::toTemplateVariableMongo)
				.collect(Collectors.toList());
	}

	private TemplateVariableMongo toTemplateVariableMongo(WritableTemplateVariable templateVariable) {
		return TemplateVariableMongo.builder()
				.id(templateVariable.getId())
				.name(templateVariable.getName())
				.label(templateVariable.getLabel())
				.values(templateVariable.getValues())
				.useValues(templateVariable.isUseValues())
				.defaultValue(templateVariable.getDefaultValue())
				.showOnDashboard(templateVariable.isShowOnDashboard())
				.build();
	}

	private ReadableTemplateVariable fromTemplateVariableMongo(TemplateVariableMongo templateVariable) {
		return new ReadableTemplateVariable(templateVariable.getId(),
				templateVariable.getName(),
				templateVariable.getLabel(),
				templateVariable.getValues(),
				templateVariable.isUseValues(),
				templateVariable.getDefaultValue(),
				templateVariable.isShowOnDashboard());
	}

	private ProjectVersionMongo toProjectVersionMongo(WritableProjectVersion version) {
		ProjectVersionMongo versionMongo = new ProjectVersionMongo();
		versionMongo.setName(version.getName());
		versionMongo.setProjectVersionUuid(version.getUuid());
		versionMongo.setDeploymentBehaviour(version.getDeploymentBehaviour());
		versionMongo.setTemplateVariables(version.getTemplateVariables());
		versionMongo.setDockerContentDigest(version.getDockerContentDigest());
		versionMongo.setUrls(version.getUrls());
		versionMongo.setOutdated(version.isOutdated());
		versionMongo.setConfigurationTemplates(ConfigurationTemplateMongoMapper.toConfigurationTemplateMongos(version.getConfigurationTemplates()));
		versionMongo.setLifetimeBehaviour(version.getLifetimeBehaviour().orElse(null));
		versionMongo.setNamespace(version.getNamespace());
		versionMongo.setDesiredState(version.getDesiredState());
		versionMongo.setImageUpdatedDate(version.getImageUpdatedDate());

		return versionMongo;
	}

	private ReadableProject fromProjectMongo(ProjectMongo projectMongo) {
		List<ReadableProjectVersion> versions =
				projectMongo.getVersions()
						.stream()
						.map(this::fromProjectVersionMongo)
						.collect(Collectors.toList());

		return ReadableProject.builder()
				.id(projectMongo.getProjectUuid())
				.name(projectMongo.getName())
				.imageName(projectMongo.getImageName())
				.newVersionsDeploymentBehaviour(projectMongo.getNewVersionsDeploymentBehaviour())
				.defaultConfigurationTemplates(ConfigurationTemplateMongoMapper.fromConfigurationTemplateMongos(projectMongo.getDefaultConfigurationTemplates()))
				.templateVariables(fromTemplateVariablesMongo(projectMongo.getTemplateVariables()))
				.dockerRegistryId(projectMongo.getDockerRegistryUUID())
				.versions(versions)
				.defaultLifetimeBehaviour(projectMongo.getDefaultLifetimeBehaviour())
				.namespace(projectMongo.getNamespace())
				.build();
	}

	private ReadableProjectVersion fromProjectVersionMongo(ProjectVersionMongo versionMongo) {
		return ReadableProjectVersion.builder()
				.uuid(versionMongo.getProjectVersionUuid())
				.name(versionMongo.getName())
				.deploymentBehaviour(versionMongo.getDeploymentBehaviour())
				.templateVariables(versionMongo.getTemplateVariables())
				.dockerContentDigest(versionMongo.getDockerContentDigest())
				.urls(versionMongo.getUrls())
				.outdated(versionMongo.isOutdated())
				.configurationTemplates(ConfigurationTemplateMongoMapper.fromConfigurationTemplateMongos(versionMongo.getConfigurationTemplates()))
				.lifetimeBehaviour(versionMongo.getLifetimeBehaviour())
				.desiredState(versionMongo.getDesiredState())
				.imageUpdatedDate(versionMongo.getImageUpdatedDate())
				.namespace(versionMongo.getNamespace()).build();
	}
}
