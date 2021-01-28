package io.oneko.kubernetes;

import io.oneko.namespace.DefinedNamespace;

public interface NamespaceManager {

	void createNamespaceAndAddImagePullSecrets(DefinedNamespace namespace);

	void deleteNamespace(DefinedNamespace namespace);

}
