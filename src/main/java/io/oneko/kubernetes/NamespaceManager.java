package io.oneko.kubernetes;

import io.oneko.docker.DockerRegistry;
import io.oneko.namespace.Namespace;

public interface NamespaceManager {

	void createNamespaceAndAddImagePullSecrets(Namespace namespace);

	void deleteNamespace(Namespace namespace);

	void updateImagePullSecretsWithRegistry(DockerRegistry dockerRegistry);

	void removeImagePullSecretsForRegistry(DockerRegistry dockerRegistry);

}
