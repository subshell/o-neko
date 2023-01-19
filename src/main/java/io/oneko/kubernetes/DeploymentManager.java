package io.oneko.kubernetes;

import java.util.concurrent.Future;

import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.WritableProjectVersion;

/**
 * Deals with deployments of ProjectVersions counter parts on the kubernetes side.
 */
public interface DeploymentManager {

	ReadableProjectVersion deploy(WritableProjectVersion version);

	Future<ReadableProjectVersion> deployAsync(WritableProjectVersion version);

	ReadableProjectVersion stopDeployment(WritableProjectVersion version);

	Future<ReadableProjectVersion> stopDeploymentAsync(WritableProjectVersion version);

}
