package io.oneko.namespace;

import java.util.UUID;

public interface Namespace {

	UUID getId();
	
	/**
	 * Provides the String-pendant of this namespace as required by kubernetes.
	 * All instances guarantee to be legal as defined by {@link NamespaceConventions#NAMESPACE_REGEX}.
	 */
	String asKubernetesNameSpace();
}
