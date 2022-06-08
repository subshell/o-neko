package io.oneko.kubernetes;

import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.WritableProjectVersion;

/**
 * Deals with deployments of ProjectVersions counter parts on the kubernetes side.
 */
public interface DeploymentManager {

	ReadableProjectVersion deploy(WritableProjectVersion version);

	ReadableProjectVersion stopDeployment(WritableProjectVersion version);

	void rollback(WritableProjectVersion version);

}
