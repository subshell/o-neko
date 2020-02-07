package io.oneko.docker;

import io.oneko.domain.ModificationAware;

import java.util.Set;
import java.util.UUID;

public class WritableDockerRegistry extends DockerRegistry implements ModificationAware {

	/**
	 * Creates a completely new DockerRegistry
	 */
	public WritableDockerRegistry() {
		this.uuid.set(UUID.randomUUID());
	}

	public WritableDockerRegistry(UUID uuid, String name, String registryUrl, String userName, String password, boolean trustInsecureCertificate) {
		super(uuid, name, registryUrl, userName, password, trustInsecureCertificate);
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

	public DockerRegistry readable() {
		return new DockerRegistry(getUuid(), getName(), getRegistryUrl(), getUserName(), getPassword(), isTrustInsecureCertificate());
	}

	@Override
	public void touch() {
		modifications.touch();
	}

	@Override
	public boolean isDirty() {
		return modifications.isDirty();
	}

	@Override
	public Set<String> getDirtyProperties() {
		return modifications.getDirtyProperties();
	}
}
