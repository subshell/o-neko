package io.oneko.namespace;

import java.util.UUID;

import io.oneko.domain.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
public class ReadableNamespace extends Identifiable implements Namespace {

	@Getter
	private final UUID id;
	private final String name;

	@Override
	public String asKubernetesNameSpace() {
		return name;
	}

	public WritableNamespace writable() {
		return new WritableNamespace(getId(), asKubernetesNameSpace());
	}
}
