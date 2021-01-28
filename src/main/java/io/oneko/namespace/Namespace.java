package io.oneko.namespace;

public interface Namespace {

	/**
	 * Provides the String-pendant of this namespace as required by kubernetes.
	 * All instances guarantee to be legal as defined by {@link NamespaceConventions#NAMESPACE_REGEX}.
	 */
	String asKubernetesNameSpace();
}
