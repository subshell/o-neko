package io.oneko.kubernetes;

import java.util.concurrent.CompletableFuture;

import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.WritableProjectVersion;

/**
 * Deals with deployments of ProjectVersions counter parts on the kubernetes side.
 */
public interface DeploymentManager {

	ReadableProjectVersion deploy(WritableProjectVersion version);

	CompletableFuture<ReadableProjectVersion> deployAsync(WritableProjectVersion version);

	ReadableProjectVersion stopDeployment(WritableProjectVersion version);

	CompletableFuture<ReadableProjectVersion> stopDeploymentAsync(WritableProjectVersion version);

}
