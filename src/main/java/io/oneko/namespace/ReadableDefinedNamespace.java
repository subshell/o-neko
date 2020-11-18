package io.oneko.namespace;

import io.oneko.domain.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

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
