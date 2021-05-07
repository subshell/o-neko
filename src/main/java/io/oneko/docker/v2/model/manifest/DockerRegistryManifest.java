package io.oneko.docker.v2.model.manifest;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerRegistryManifest {

	@Data
	static class Config {
		String digest;
	}

	@Data
	public static class Digest {
		String algorithm;
		String digest;

		public Digest(String digest) {
			final String[] split = StringUtils.defaultString(digest).split(":");
			this.algorithm = StringUtils.defaultString(split[0]);
			this.digest = StringUtils.defaultString(split[1]);
		}

		public String getFullDigest() {
			return algorithm + ":" + digest;
		}
	}

	private Config config;

	public Digest getDigest() {
		return new Digest(config.digest);
	}
}
