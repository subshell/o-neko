package io.oneko.kubernetes;

import io.oneko.project.ProjectVersion;
import io.oneko.projectmesh.MeshComponent;
import io.oneko.projectmesh.ProjectMesh;
import reactor.core.publisher.Mono;

/**
 * Deals with deployments of ProjectVersions counter parts on the kubernetes side.
 */
public interface KubernetesDeploymentManager {

	Mono<ProjectVersion> deploy(ProjectVersion version);

	Mono<ProjectMesh> deploy(ProjectMesh mesh);

	Mono<MeshComponent> deploy(MeshComponent mesh);

	Mono<ProjectVersion> stopDeployment(ProjectVersion version);

	Mono<ProjectMesh> stopDeployment(ProjectMesh mesh);

	Mono<ProjectMesh> stopDeployment(MeshComponent mesh);
}
