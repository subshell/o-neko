package io.oneko.docker;

import java.util.UUID;

import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareProperty;
import lombok.Builder;

public class WritableDockerRegistry extends ModificationAwareIdentifiable implements DockerRegistry {

	private final ModificationAwareProperty<UUID> uuid = new ModificationAwareProperty<>(this, "uuid");
	private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private final ModificationAwareProperty<String> registryUrl = new ModificationAwareProperty<>(this, "registryUrl");
	private final ModificationAwareProperty<String> userName = new ModificationAwareProperty<>(this, "userName");
	private final ModificationAwareProperty<String> password = new ModificationAwareProperty<>(this, "password");
	private final ModificationAwareProperty<Boolean> trustInsecureCertificate = new ModificationAwareProperty<>(this, "trustInsecureCertificate");

	/**
	 * Creates a completely new DockerRegistry
	 */
	public WritableDockerRegistry() {
		this.uuid.set(UUID.randomUUID());
		this.trustInsecureCertificate.set(false);//init primitive values
	}

	@Builder
	public WritableDockerRegistry(UUID uuid, String name, String registryUrl, String userName, String password, boolean trustInsecureCertificate) {
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

	public void setName(String name) {
		this.name.set(name);
	}

	public void setRegistryUrl(String registryUrl) {
		this.registryUrl.set(registryUrl);
	}

	public void setUserName(String userName) {
		this.userName.set(userName);
	}

	public void setPassword(String password) {
		this.password.set(password);
	}

	public void setTrustInsecureCertificate(boolean trustInsecureCertificate) {
		this.trustInsecureCertificate.set(trustInsecureCertificate);
	}

	public ReadableDockerRegistry readable() {
		return new ReadableDockerRegistry(getUuid(), getName(), getRegistryUrl(), getUserName(), getPassword(), isTrustInsecureCertificate());
	}

}
