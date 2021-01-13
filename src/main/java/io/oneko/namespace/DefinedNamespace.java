package io.oneko.namespace;

import java.util.UUID;

/**
 * This is an explicitly defined namespace that can be used for project versions.
 */
public interface DefinedNamespace extends Namespace {

	UUID getId();

	String asKubernetesNameSpace();

}
