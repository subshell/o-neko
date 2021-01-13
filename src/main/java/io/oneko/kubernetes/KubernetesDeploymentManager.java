package io.oneko.kubernetes;

import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.WritableProjectVersion;
import io.oneko.projectmesh.ReadableMeshComponent;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableMeshComponent;
import io.oneko.projectmesh.WritableProjectMesh;

/**
 * Deals with deployments of ProjectVersions counter parts on the kubernetes side.
 */
public interface KubernetesDeploymentManager {

	ReadableProjectVersion deploy(WritableProjectVersion version);

	ReadableProjectMesh deploy(WritableProjectMesh mesh);

	ReadableMeshComponent deploy(WritableMeshComponent mesh);

	ReadableProjectVersion stopDeployment(WritableProjectVersion version);

	ReadableProjectMesh stopDeployment(WritableProjectMesh mesh);

	ReadableProjectMesh stopDeployment(WritableMeshComponent mesh);
}
