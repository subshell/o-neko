package io.oneko.namespace;

public interface WritableHasNamespace extends HasNamespace {

	void assignDefinedNamespace(DefinedNamespace namespace);

	ImplicitNamespace resetToImplicitNamespace();

}
