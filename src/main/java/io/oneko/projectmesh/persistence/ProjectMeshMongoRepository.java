package io.oneko.projectmesh.persistence;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.DefinedNamespaceRepository;
import io.oneko.namespace.ReadableDefinedNamespace;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.persistence.ConfigurationTemplateMongoMapper;
import io.oneko.projectmesh.*;
import io.oneko.projectmesh.event.EventAwareProjectMeshRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
	public Optional<ReadableProjectMesh> getById(UUID id) {
		return this.innerRepo.findById(id).map(this::fromMongo);
	}

	@Override
	public Optional<ReadableProjectMesh> getByName(String name) {
		return this.innerRepo.findByName(name).map(this::fromMongo);
	}

	@Override
	public List<ReadableProjectMesh> getAll() {
		return this.innerRepo.findAll().stream().map(this::fromMongo).collect(Collectors.toList());
	}

	@Override
	protected ReadableProjectMesh addInternally(WritableProjectMesh project) {
		return fromMongo(this.innerRepo.save(toMongo(project)));
	}

	@Override
	protected void removeInternally(ProjectMesh<?, ?> mesh) {
		this.innerRepo.deleteById(mesh.getId());
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

	private ReadableProjectMesh fromMongo(ProjectMeshMongo mongo) {
		ReadableDefinedNamespace readableDefinedNamespace = Optional.ofNullable(mongo.getNamespace()).flatMap(definedNamespaceRepository::getById).orElse(null);
		return fromMongo(mongo, readableDefinedNamespace);
	}

	private ReadableProjectMesh fromMongo(ProjectMeshMongo mongo, DefinedNamespace namespace) {
		List<ReadableMeshComponent> components = mongo.getComponents().stream().map(this::fromMongo).collect(Collectors.toList());
		return ReadableProjectMesh.builder()
				.id(mongo.getId())
				.name(mongo.getName())
				.deploymentBehaviour(mongo.getDeploymentBehaviour())
				.lifetimeBehaviour(mongo.getLifetimeBehaviour())
				.namespace(namespace)
				.components(components)
				.build();
	}

	private ReadableMeshComponent fromMongo(MeshComponentMongo mongo) {
		ReadableProjectVersion readableProjectVersion = this.projectRepository.getById(mongo.getProjectId()).flatMap(project -> project.getVersionById(mongo.getProjectVersionId())).orElse(null);
		//TODO: what if the version does not exist any longer?
		return fromMongo(mongo, readableProjectVersion);
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
