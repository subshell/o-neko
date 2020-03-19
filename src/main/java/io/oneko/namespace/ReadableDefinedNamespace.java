package io.oneko.namespace;

import java.util.UUID;

import io.oneko.domain.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
public class ReadableDefinedNamespace extends Identifiable implements DefinedNamespace {

	@Getter
	private final UUID id;
	private final String name;

	@Override
	public String asKubernetesNameSpace() {
		return name;
	}

	public WritableDefinedNamespace writable() {
		return new WritableDefinedNamespace(getId(), asKubernetesNameSpace());
	}
}
