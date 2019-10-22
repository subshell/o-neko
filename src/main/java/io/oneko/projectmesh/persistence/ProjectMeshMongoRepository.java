package io.oneko.projectmesh.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.oneko.event.EventDispatcher;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.DefinedNamespaceRepository;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.project.persistence.ConfigurationTemplateMongoMapper;
import io.oneko.projectmesh.MeshComponent;
import io.oneko.projectmesh.ProjectMesh;
import io.oneko.projectmesh.event.EventAwareProjectMeshRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
class ProjectMeshMongoRepository extends EventAwareProjectMeshRepository {

	private final ProjectMeshMongoSpringRepository innerRepo;
	private final DefinedNamespaceRepository definedNamespaceRepository;
	private final ProjectRepository projectRepository;

	ProjectMeshMongoRepository(DefinedNamespaceRepository definedNamespaceRepository, EventDispatcher eventDispatcher, ProjectMeshMongoSpringRepository innerRepo, ProjectRepository projectRepository) {
		super(eventDispatcher);
		this.innerRepo = innerRepo;
		this.definedNamespaceRepository = definedNamespaceRepository;
		this.projectRepository = projectRepository;
	}

	@Override
	public Mono<ProjectMesh> getById(UUID id) {
		return this.innerRepo.findById(id).flatMap(this::fromMongo);
	}

	@Override
	public Mono<ProjectMesh> getByName(String name) {
		return this.innerRepo.findByName(name).flatMap(this::fromMongo);
	}

	@Override
	public Flux<ProjectMesh> getAll() {
		return this.innerRepo.findAll().flatMap(this::fromMongo);
	}

	@Override
	protected Mono<ProjectMesh> addInternally(ProjectMesh project) {
		return this.innerRepo.save(toMongo(project)).flatMap(this::fromMongo);
	}

	@Override
	protected Mono<Void> removeInternally(ProjectMesh mesh) {
		return this.innerRepo.deleteById(mesh.getId());
	}

	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Mapping stuff
     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

	private ProjectMeshMongo toMongo(ProjectMesh mesh) {
		ProjectMeshMongo mongo = new ProjectMeshMongo();
		mongo.setId(mesh.getId());
		mongo.setName(mesh.getName());
		mongo.setNamespace(mesh.getDefinedNamespaceId());
		mongo.setDeploymentBehaviour(mesh.getDeploymentBehaviour());
		mongo.setLifetimeBehaviour(mesh.getLifetimeBehaviour().orElse(null));
		mongo.setComponents(mesh.getComponents().stream().map(this::toMongo).collect(Collectors.toList()));
		return mongo;
	}

	private MeshComponentMongo toMongo(MeshComponent meshComponent) {
		MeshComponentMongo mongo = new MeshComponentMongo();
		mongo.setId(meshComponent.getId());
		mongo.setName(meshComponent.getName());
		mongo.setProjectId(meshComponent.getProject().getId());
		mongo.setProjectVersionId(meshComponent.getProjectVersion().getId());
		mongo.setDockerContentDigest(meshComponent.getDockerContentDigest());
		mongo.setTemplateVariables(meshComponent.getTemplateVariables());
		mongo.setConfigurationTemplates(ConfigurationTemplateMongoMapper.toConfigurationTemplateMongos(meshComponent.getConfigurationTemplates()));
		mongo.setOutdated(meshComponent.isOutdated());
		mongo.setUrls(meshComponent.getUrls());
		mongo.setDesiredState(meshComponent.getDesiredState());
		return mongo;
	}

	private Mono<ProjectMesh> fromMongo(ProjectMeshMongo mongo) {
		UUID namespaceId = mongo.getNamespace();
		if (namespaceId != null) {
			return definedNamespaceRepository.getById(namespaceId)
					.flatMap(namespace -> fromMongo(mongo, namespace));
		} else {
			return fromMongo(mongo, null);
		}
	}

	private Mono<ProjectMesh> fromMongo(ProjectMeshMongo mongo, DefinedNamespace namespace) {
		List<MeshComponent> components = new ArrayList<>();
		final ProjectMesh mesh = ProjectMesh.builder()
				.id(mongo.getId())
				.name(mongo.getName())
				.deploymentBehaviour(mongo.getDeploymentBehaviour())
				.lifetimeBehaviour(mongo.getLifetimeBehaviour())
				.namespace(namespace)
				.components(components)
				.build();

		Flux<MeshComponent> componentsFlux = Flux.concat(
				mongo.getComponents()
						.stream()
						.map(cm -> fromMongo(mesh, cm))
						.collect(Collectors.toList()));

		return componentsFlux.doOnNext(components::add)
				.then(Mono.just(mesh));
	}

	private Mono<MeshComponent> fromMongo(ProjectMesh owner, MeshComponentMongo mongo) {
		return this.projectRepository.getById(mongo.getProjectId())
				.map(project -> project.getVersionByUUID(mongo.getProjectVersionId()))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(component -> fromMongo(owner, mongo, component));
	}

	private MeshComponent fromMongo(ProjectMesh owner, MeshComponentMongo mongo, ProjectVersion version) {
		return MeshComponent.builder()
				.owner(owner)
				.id(mongo.getId())
				.name(mongo.getName())
				.project(version.getProject())
				.version(version)
				.dockerContentDigest(mongo.getDockerContentDigest())
				.templateVariables(mongo.getTemplateVariables())
				.configurationTemplates(ConfigurationTemplateMongoMapper.fromConfigurationTemplateMongos(mongo.getConfigurationTemplates()))
				.outdated(mongo.isOutdated())
				.urls(mongo.getUrls())
				.desiredState(mongo.getDesiredState())
				.build();
	}

}
