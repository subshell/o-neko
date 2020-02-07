package io.oneko.docker;

import java.util.UUID;

import io.oneko.domain.Identifiable;
import io.oneko.domain.ModificationAwareContainer;
import io.oneko.domain.ModificationAwareProperty;
import lombok.Builder;

public class DockerRegistry extends Identifiable {

	protected final ModificationAwareContainer modifications = new ModificationAwareContainer();
	protected final ModificationAwareProperty<UUID> uuid = new ModificationAwareProperty<>(modifications, "uuid");
	protected final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(modifications, "name");
	protected final ModificationAwareProperty<String> registryUrl = new ModificationAwareProperty<>(modifications, "registryUrl");
	protected final ModificationAwareProperty<String> userName = new ModificationAwareProperty<>(modifications, "userName");
	protected final ModificationAwareProperty<String> password = new ModificationAwareProperty<>(modifications, "password");
	protected final ModificationAwareProperty<Boolean> trustInsecureCertificate = new ModificationAwareProperty<>(modifications, "trustInsecureCertificate");

	/**
	 * Creates a completely new DockerRegistry
	 */
	public DockerRegistry() {
		this.uuid.set(UUID.randomUUID());
		this.trustInsecureCertificate.set(false);//init primitive values
	}

	@Builder
	public DockerRegistry(UUID uuid, String name, String registryUrl, String userName, String password, boolean trustInsecureCertificate) {
		this.uuid.init(uuid);
		this.name.init(name);
		this.registryUrl.init(registryUrl);
		this.userName.init(userName);
		this.password.init(password);
		this.trustInsecureCertificate.init(trustInsecureCertificate);
	}

	public UUID getUuid() {
		return uuid.get();
	}

	@Override
	public UUID getId() {
		return this.uuid.get();
	}

	public String getName() {
		return name.get();
	}

	public String getRegistryUrl() {
		return registryUrl.get();
	}

	public String getUserName() {
		return this.userName.get();
	}

	public String getPassword() {
		return password.get();
	}

	public boolean isTrustInsecureCertificate() {
		return trustInsecureCertificate.get();
	}

	public WritableDockerRegistry writable() {
		return new WritableDockerRegistry(getUuid(), getName(), getRegistryUrl(), getUserName(), getPassword(), isTrustInsecureCertificate());
	}

}
