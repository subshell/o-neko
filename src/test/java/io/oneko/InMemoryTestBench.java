package io.oneko;

import io.oneko.docker.DockerRegistryRepository;
import io.oneko.docker.persistence.DockerRegistryInMemoryRepository;
import io.oneko.event.CurrentEventTrigger;
import io.oneko.event.EventDispatcher;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import io.oneko.kubernetes.deployments.persistence.DeploymentInMemoryRepository;
import io.oneko.namespace.DefinedNamespaceRepository;
import io.oneko.namespace.persistence.DefinedNamespaceInMemoryRepository;
import io.oneko.project.ProjectRepository;
import io.oneko.project.persistence.ProjectInMemoryRepository;
import io.oneko.projectmesh.MeshService;
import io.oneko.projectmesh.ProjectMeshRepository;
import io.oneko.projectmesh.persistence.ProjectMeshInMemoryRepository;
import io.oneko.user.UserRepository;
import io.oneko.user.persistence.UserInMemoryRepository;

/**
 * Prepared test setup with in memory implementations of all the repositories.
 */
public class InMemoryTestBench {
    private InMemoryTestBench(){}
    public final CurrentEventTrigger currentEventTrigger = new CurrentEventTrigger();
    public final EventDispatcher eventDispatcher = new EventDispatcher(currentEventTrigger);

    public final DockerRegistryRepository dockerRegistryRepository = new DockerRegistryInMemoryRepository(eventDispatcher);
    public final ProjectRepository projectRepository = new ProjectInMemoryRepository(eventDispatcher);
    public final ProjectMeshRepository projectMeshRepository = new ProjectMeshInMemoryRepository(eventDispatcher);
    public final UserRepository userRepository = new UserInMemoryRepository(eventDispatcher);
    public final DefinedNamespaceRepository definedNamespaceRepository = new DefinedNamespaceInMemoryRepository(eventDispatcher);
    public final DeploymentRepository deploymentRepository = new DeploymentInMemoryRepository();

    public final MeshService meshService = new MeshService(projectRepository);

    /**
     * Provides a new empty test bench with a bunch of in memory repos.
     */
    public static InMemoryTestBench empty() {
        return new InMemoryTestBench();
    }
}
