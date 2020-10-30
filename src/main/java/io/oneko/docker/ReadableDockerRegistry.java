package io.oneko.docker;

import java.util.UUID;

import io.oneko.domain.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class ReadableDockerRegistry extends Identifiable implements DockerRegistry {

	private final UUID uuid;
	private final String name;
	private final String registryUrl;
	private final String userName;
	private final String password;
	private final boolean trustInsecureCertificate;

	@Override
	public UUID getId() {
		return uuid;
	}

	public WritableDockerRegistry writable() {
		return new WritableDockerRegistry(getUuid(), getName(), getRegistryUrl(), getUserName(), getPassword(), isTrustInsecureCertificate());
	}
}
