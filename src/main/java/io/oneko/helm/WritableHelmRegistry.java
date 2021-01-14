package io.oneko.helm;

import java.util.UUID;

import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareProperty;
import lombok.Builder;

public class WritableHelmRegistry extends ModificationAwareIdentifiable implements HelmRegistry {

	private final ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
	private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private final ModificationAwareProperty<String> url = new ModificationAwareProperty<>(this, "url");
	private final ModificationAwareProperty<String> username = new ModificationAwareProperty<>(this, "username");
	private final ModificationAwareProperty<String> password = new ModificationAwareProperty<>(this, "password");


	/**
	 * Creates a completely new Helm Registry
	 */
	public WritableHelmRegistry() {
		this.id.set(UUID.randomUUID());
	}

	@Builder
	public WritableHelmRegistry(UUID id, String name, String url, String username, String password) {
		this.id.init(id);
		this.name.init(name);
		this.url.init(url);
		this.username.init(username);
		this.password.init(password);
	}

	public ReadableHelmRegistry readable() {
		return ReadableHelmRegistry.builder()
				.id(getId())
				.name(getName())
				.url(getUrl())
				.username(getUsername())
				.password(getPassword())
				.build();
	}

	@Override
	public UUID getId() {
		return id.get();
	}

	@Override
	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	@Override
	public String getUrl() {
		return url.get();
	}

	public void setUrl(String url) {
		this.url.set(url);
	}

	@Override
	public String getUsername() {
		return username.get();
	}

	public void setUsername(String username) {
		this.username.set(username);
	}

	@Override
	public String getPassword() {
		return password.get();
	}

	public void setPassword(String password) {
		this.password.set(password);
	}
}
