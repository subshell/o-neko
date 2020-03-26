package io.oneko.kubernetes;

import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.WritableProjectVersion;
import io.oneko.projectmesh.ReadableMeshComponent;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableMeshComponent;
import io.oneko.projectmesh.WritableProjectMesh;
import reactor.core.publisher.Mono;

/**
 * Deals with deployments of ProjectVersions counter parts on the kubernetes side.
 */
public interface KubernetesDeploymentManager {

	Mono<ReadableProjectVersion> deploy(WritableProjectVersion version);

	Mono<ReadableProjectMesh> deploy(WritableProjectMesh mesh);

	Mono<ReadableMeshComponent> deploy(WritableMeshComponent mesh);

	Mono<ReadableProjectVersion> stopDeployment(WritableProjectVersion version);

	Mono<ReadableProjectMesh> stopDeployment(WritableProjectMesh mesh);

	Mono<ReadableProjectMesh> stopDeployment(WritableMeshComponent mesh);
}
