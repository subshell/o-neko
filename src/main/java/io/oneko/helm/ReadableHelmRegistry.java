package io.oneko.helm;

import java.util.UUID;

import io.oneko.domain.Identifiable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReadableHelmRegistry extends Identifiable implements HelmRegistry {
	private final UUID id;
	private final String name;
	private final String url;
	private final String username;
	private final String password;

	public WritableHelmRegistry writable() {
		return WritableHelmRegistry.builder()
				.id(getId())
				.name(getName())
				.url(getUrl())
				.username(getUsername())
				.password(getPassword())
				.build();
	}
}
