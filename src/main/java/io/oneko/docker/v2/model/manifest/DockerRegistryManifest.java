package io.oneko.docker.v2.model.manifest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerRegistryManifest {

	@Data
	static class Config {
		String digest;
	}

	private Config config;

	public String getDigest() {
		return config.digest;
	}
}
