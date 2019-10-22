package io.oneko.namespace;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Every project version needs a namespace in kubernetes. If a project version has no namespace set explicitly, then it
 * will use its own implicitly generated namespace.
 */
@EqualsAndHashCode(of = "implicitName")
public class ImplicitNamespace implements Namespace {

	private final String implicitName;
	@Getter
	private final HasNamespace owner;

	public ImplicitNamespace(HasNamespace owner) {
		this.owner = owner;
		this.implicitName = NamespaceConventions.sanitizeNamespace(owner.getProtoNamespace());
	}

	@Override
	public String asKubernetesNameSpace() {
		return this.implicitName;
	}
}
