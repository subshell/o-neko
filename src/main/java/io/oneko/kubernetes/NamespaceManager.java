package io.oneko.kubernetes;

import io.oneko.namespace.Namespace;

public interface NamespaceManager {

	void createNamespaceAndAddImagePullSecrets(Namespace namespace);

	void deleteNamespace(Namespace namespace);

}
