package io.oneko.project.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.oneko.docker.DockerRegistry;
import io.oneko.docker.DockerRegistryRepository;
import io.oneko.event.EventDispatcher;
import io.oneko.namespace.DefinedNamespaceRepository;
import io.oneko.project.Project;
import io.oneko.project.ProjectVersion;
import io.oneko.project.TemplateVariable;
import io.oneko.project.event.EventAwareProjectRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
class ProjectMongoRepository extends EventAwareProjectRepository {

	private final ProjectMongoSpringRepository innerProjectRepo;
	private final DockerRegistryRepository registryRepository;
	private final DefinedNamespaceRepository definedNamespaceRepository;

	@Autowired
	ProjectMongoRepository(ProjectMongoSpringRepository innerProjectRepo, DockerRegistryRepository registryRepository, DefinedNamespaceRepository definedNamespaceRepository, EventDispatcher eventDispatcher) {
		super(eventDispatcher);
		this.innerProjectRepo = innerProjectRepo;
		this.registryRepository = registryRepository;
		this.definedNamespaceRepository = definedNamespaceRepository;
	}

	@Override
	public Mono<Project> getById(UUID projectId) {
		return this.innerProjectRepo.findById(projectId).flatMap(this::fromProjectMongo);
	}

	@Override
	public Mono<Project> getByName(String name) {
		return this.innerProjectRepo.findByName(name).flatMap(this::fromProjectMongo);
	}

	@Override
	public Flux<Project> getByDockerRegistryUuid(UUID dockerRegistryUUID) {
		return this.innerProjectRepo.findByDockerRegistryUUID(dockerRegistryUUID).flatMap(this::fromProjectMongo);
	}

	@Override
	public Flux<Project> getAll() {
		return this.innerProjectRepo.findAll().flatMap(this::fromProjectMongo);
	}

	@Override
	protected Mono<Project> addInternally(Project project) {
		ProjectMongo projectMongo = this.toProjectMongo(project);
		return this.innerProjectRepo.save(projectMongo)
				.flatMap(this::fromProjectMongo);
	}

	@Override
	protected Mono<Void> removeInternally(Project project) {
		return this.innerProjectRepo.deleteById(project.getId());
	}

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Mapping stuff
     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

	private ProjectMongo toProjectMongo(Project project) {
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
			projectMongo.setDockerRegistryUUID(project.getDockerRegistry().getUuid());
		}

		List<ProjectVersionMongo> versionsMongo = project.getVersions().stream()
				.map(this::toProjectVersionMongo)
				.collect(Collectors.toList());
		projectMongo.setVersions(versionsMongo);
		projectMongo.setDefaultLifetimeBehaviour(project.getDefaultLifetimeBehaviour().orElse(null));

		return projectMongo;
	}

	private List<TemplateVariable> fromTemplateVariablesMongo(List<TemplateVariableMongo> templateVariables) {
		return templateVariables.stream()
				.map(this::fromTemplateVariableMongo)
				.collect(Collectors.toList());
	}

	private List<TemplateVariableMongo> toTemplateVariablesMongo(List<TemplateVariable> templateVariables) {
		return templateVariables.stream()
				.map(this::toTemplateVariableMongo)
				.collect(Collectors.toList());
	}

	private TemplateVariableMongo toTemplateVariableMongo(TemplateVariable templateVariable) {
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

	private TemplateVariable fromTemplateVariableMongo(TemplateVariableMongo templateVariable) {
		return new TemplateVariable(templateVariable.getId(),
				templateVariable.getName(),
				templateVariable.getLabel(),
				templateVariable.getValues(),
				templateVariable.isUseValues(),
				templateVariable.getDefaultValue(),
				templateVariable.isShowOnDashboard());
	}

	private ProjectVersionMongo toProjectVersionMongo(ProjectVersion version) {
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

	private Mono<Project> fromProjectMongo(ProjectMongo projectMongo) {
		return this.registryRepository.getById(projectMongo.getDockerRegistryUUID())
				.flatMap(registry -> this.fromProjectMongoAndRegistry(projectMongo, registry))
				.switchIfEmpty(this.fromProjectMongoAndRegistry(projectMongo, null));
	}

	private Mono<Project> fromProjectMongoAndRegistry(ProjectMongo projectMongo, DockerRegistry registry) {
		List<ProjectVersion> versions = new ArrayList<>();

		Project project = Project.builder()
				.uuid(projectMongo.getProjectUuid())
				.name(projectMongo.getName())
				.imageName(projectMongo.getImageName())
				.newVersionsDeploymentBehaviour(projectMongo.getNewVersionsDeploymentBehaviour())
				.defaultConfigurationTemplates(ConfigurationTemplateMongoMapper.fromConfigurationTemplateMongos(projectMongo.getDefaultConfigurationTemplates()))
				.templateVariables(fromTemplateVariablesMongo(projectMongo.getTemplateVariables()))
				.dockerRegistry(registry)
				.versions(versions)
				.defaultLifetimeBehaviour(projectMongo.getDefaultLifetimeBehaviour())
				.build();

		Flux<ProjectVersion> projectVersionFlux = Flux.concat(
				projectMongo.getVersions()
						.stream()
						.map(vm -> this.fromProjectVersionMongo(project, vm))
						.collect(Collectors.toList()));

		return projectVersionFlux.doOnNext(versions::add)
				.then(Mono.just(project));
	}

	private Mono<ProjectVersion> fromProjectVersionMongo(Project project, ProjectVersionMongo versionMongo) {
		ProjectVersion.ProjectVersionBuilder projectVersionBuilder = ProjectVersion.builder()
				.uuid(versionMongo.getProjectVersionUuid())
				.name(versionMongo.getName())
				.deploymentBehaviour(versionMongo.getDeploymentBehaviour())
				.templateVariables(versionMongo.getTemplateVariables())
				.project(project)
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
