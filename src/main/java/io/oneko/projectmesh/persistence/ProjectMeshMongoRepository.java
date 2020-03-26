package io.oneko.projectmesh.persistence;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.DefinedNamespaceRepository;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.persistence.ConfigurationTemplateMongoMapper;
import io.oneko.projectmesh.ProjectMesh;
import io.oneko.projectmesh.ReadableMeshComponent;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableMeshComponent;
import io.oneko.projectmesh.WritableProjectMesh;
import io.oneko.projectmesh.event.EventAwareProjectMeshRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Profile(Profiles.MONGO)
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
	public Mono<ReadableProjectMesh> getById(UUID id) {
		return this.innerRepo.findById(id).flatMap(this::fromMongo);
	}

	@Override
	public Mono<ReadableProjectMesh> getByName(String name) {
		return this.innerRepo.findByName(name).flatMap(this::fromMongo);
	}

	@Override
	public Flux<ReadableProjectMesh> getAll() {
		return this.innerRepo.findAll().flatMap(this::fromMongo);
	}

	@Override
	protected Mono<ReadableProjectMesh> addInternally(WritableProjectMesh project) {
		return this.innerRepo.save(toMongo(project)).flatMap(this::fromMongo);
	}

	@Override
	protected Mono<Void> removeInternally(ProjectMesh<?, ?> mesh) {
		return this.innerRepo.deleteById(mesh.getId());
	}

	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Mapping stuff
     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

	private ProjectMeshMongo toMongo(WritableProjectMesh mesh) {
		ProjectMeshMongo mongo = new ProjectMeshMongo();
		mongo.setId(mesh.getId());
		mongo.setName(mesh.getName());
		mongo.setNamespace(mesh.getDefinedNamespaceId());
		mongo.setDeploymentBehaviour(mesh.getDeploymentBehaviour());
		mongo.setLifetimeBehaviour(mesh.getLifetimeBehaviour().orElse(null));
		mongo.setComponents(mesh.getComponents().stream().map(this::toMongo).collect(Collectors.toList()));
		return mongo;
	}

	private MeshComponentMongo toMongo(WritableMeshComponent meshComponent) {
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

	private Mono<ReadableProjectMesh> fromMongo(ProjectMeshMongo mongo) {
		UUID namespaceId = mongo.getNamespace();
		if (namespaceId != null) {
			return definedNamespaceRepository.getById(namespaceId)
					.flatMap(namespace -> fromMongo(mongo, namespace));
		} else {
			return fromMongo(mongo, null);
		}
	}

	private Mono<ReadableProjectMesh> fromMongo(ProjectMeshMongo mongo, DefinedNamespace namespace) {
		Flux<ReadableMeshComponent> componentsFlux = Flux.concat(
				mongo.getComponents()
						.stream()
						.map(this::fromMongo)
						.collect(Collectors.toList()));

		return componentsFlux.collectList()
				.map(components ->  ReadableProjectMesh.builder()
						.id(mongo.getId())
						.name(mongo.getName())
						.deploymentBehaviour(mongo.getDeploymentBehaviour())
						.lifetimeBehaviour(mongo.getLifetimeBehaviour())
						.namespace(namespace)
						.components(components)
						.build());
	}

	private Mono<ReadableMeshComponent> fromMongo(MeshComponentMongo mongo) {
		return this.projectRepository.getById(mongo.getProjectId())
				.map(project -> project.getVersionByUUID(mongo.getProjectVersionId()))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(version -> fromMongo(mongo, version));
	}

	private ReadableMeshComponent fromMongo(MeshComponentMongo mongo, ReadableProjectVersion version) {
		return ReadableMeshComponent.builder()
				.id(mongo.getId())
				.name(mongo.getName())
				.project(version.getProject())
				.projectVersion(version)
				.dockerContentDigest(mongo.getDockerContentDigest())
				.templateVariables(mongo.getTemplateVariables())
				.configurationTemplates(ConfigurationTemplateMongoMapper.fromConfigurationTemplateMongos(mongo.getConfigurationTemplates()))
				.outdated(mongo.isOutdated())
				.urls(mongo.getUrls())
				.desiredState(mongo.getDesiredState())
				.build();
	}

}
