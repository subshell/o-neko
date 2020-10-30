package io.oneko.project.persistence;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.namespace.DefinedNamespaceRepository;
import io.oneko.project.Project;
import io.oneko.project.ReadableProject;
import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.ReadableTemplateVariable;
import io.oneko.project.WritableProject;
import io.oneko.project.WritableProjectVersion;
import io.oneko.project.WritableTemplateVariable;
import io.oneko.project.event.EventAwareProjectRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Profile(Profiles.MONGO)
class ProjectMongoRepository extends EventAwareProjectRepository {

	private final ProjectMongoSpringRepository innerProjectRepo;
	private final DefinedNamespaceRepository definedNamespaceRepository;

	@Autowired
	ProjectMongoRepository(ProjectMongoSpringRepository innerProjectRepo, DefinedNamespaceRepository definedNamespaceRepository, EventDispatcher eventDispatcher) {
		super(eventDispatcher);
		this.innerProjectRepo = innerProjectRepo;
		this.definedNamespaceRepository = definedNamespaceRepository;
	}

	@Override
	public Mono<ReadableProject> getById(UUID projectId) {
		return this.innerProjectRepo.findById(projectId).flatMap(this::fromProjectMongo);
	}

	@Override
	public Mono<ReadableProject> getByName(String name) {
		return this.innerProjectRepo.findByName(name).flatMap(this::fromProjectMongo);
	}

	@Override
	public Flux<ReadableProject> getByDockerRegistryUuid(UUID dockerRegistryUUID) {
		return this.innerProjectRepo.findByDockerRegistryUUID(dockerRegistryUUID).flatMap(this::fromProjectMongo);
	}

	@Override
	public Flux<ReadableProject> getAll() {
		return this.innerProjectRepo.findAll().flatMap(this::fromProjectMongo);
	}

	@Override
	protected Mono<ReadableProject> addInternally(WritableProject project) {
		ProjectMongo projectMongo = this.toProjectMongo(project);
		return this.innerProjectRepo.save(projectMongo)
				.flatMap(this::fromProjectMongo);
	}

	@Override
	protected Mono<Void> removeInternally(Project<?, ?> project) {
		return this.innerProjectRepo.deleteById(project.getId());
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
		versionMongo.setNamespace(version.getDefinedNamespaceId());
		versionMongo.setDesiredState(version.getDesiredState());
		versionMongo.setImageUpdatedDate(version.getImageUpdatedDate());

		return versionMongo;
	}

	private Mono<ReadableProject> fromProjectMongo(ProjectMongo projectMongo) {
		Flux<ReadableProjectVersion> projectVersionFlux = Flux.concat(
				projectMongo.getVersions()
						.stream()
						.map(this::fromProjectVersionMongo)
						.collect(Collectors.toList()));

		return projectVersionFlux.collectList()
				.map(versions -> ReadableProject.builder()
						.id(projectMongo.getProjectUuid())
						.name(projectMongo.getName())
						.imageName(projectMongo.getImageName())
						.newVersionsDeploymentBehaviour(projectMongo.getNewVersionsDeploymentBehaviour())
						.defaultConfigurationTemplates(ConfigurationTemplateMongoMapper.fromConfigurationTemplateMongos(projectMongo.getDefaultConfigurationTemplates()))
						.templateVariables(fromTemplateVariablesMongo(projectMongo.getTemplateVariables()))
						.dockerRegistryId(projectMongo.getDockerRegistryUUID())
						.versions(versions)
						.defaultLifetimeBehaviour(projectMongo.getDefaultLifetimeBehaviour())
						.build());
	}

	private Mono<ReadableProjectVersion> fromProjectVersionMongo(ProjectVersionMongo versionMongo) {
		ReadableProjectVersion.ReadableProjectVersionBuilder projectVersionBuilder = ReadableProjectVersion.builder()
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
				.imageUpdatedDate(versionMongo.getImageUpdatedDate());

		UUID namespaceId = versionMongo.getNamespace();
		if (namespaceId != null) {
			return definedNamespaceRepository.getById(namespaceId)
					.map(namespace -> projectVersionBuilder.namespace(namespace).build())
					.switchIfEmpty(Mono.just(projectVersionBuilder.build()));
		}

		return Mono.just(projectVersionBuilder.build());
	}
}
