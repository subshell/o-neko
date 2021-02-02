package io.oneko.kubernetes;

import io.oneko.docker.DockerRegistry;

public class KubernetesConventions {
	private KubernetesConventions() {
	}

	/**
	 * Returns the name for this docker registry to be used in secrets.
	 */
	public static String secretName(DockerRegistry registry) {
		String s = registry.getName().toLowerCase();
		s = s.replaceAll("_", "-");
		return s;
	}
}
