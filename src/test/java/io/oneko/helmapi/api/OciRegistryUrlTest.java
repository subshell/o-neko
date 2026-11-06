package io.oneko.helmapi.api;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OciRegistryUrlTest {

	@Test
	void explicitPlainHttpSchemeIsRecognizedWithoutProbing() {
		var registryUrl = OciRegistryUrl.resolve("oci://http://localhost:5001/some/path");

		assertThat(registryUrl.plainHttp()).isTrue();
		assertThat(registryUrl.host()).isEqualTo("localhost:5001");
		assertThat(registryUrl.repositoryPathPrefix()).isEqualTo("some/path");
		assertThat(registryUrl.getHostAndPath()).isEqualTo("localhost:5001/some/path");
	}

	@Test
	void explicitHttpsSchemeIsRecognizedWithoutProbing() {
		var registryUrl = OciRegistryUrl.resolve("oci://https://registry.example.com/some/path");

		assertThat(registryUrl.plainHttp()).isFalse();
		assertThat(registryUrl.host()).isEqualTo("registry.example.com");
		assertThat(registryUrl.getHostAndPath()).isEqualTo("registry.example.com/some/path");
	}

	@Test
	void noSchemeAndNoPathFallsBackToProbingAndYieldsPlainHttpWhenUnreachable() {
		var registryUrl = OciRegistryUrl.resolve("oci://127.0.0.1:1");

		assertThat(registryUrl.plainHttp()).isTrue();
		assertThat(registryUrl.host()).isEqualTo("127.0.0.1:1");
		assertThat(registryUrl.repositoryPathPrefix()).isEmpty();
		assertThat(registryUrl.getHostAndPath()).isEqualTo("127.0.0.1:1");
	}

	@Test
	void hostAndPathStripsSchemesWithoutProbing() {
		assertThat(OciRegistryUrl.hostAndPath("oci://http://localhost:5001/foo")).isEqualTo("localhost:5001/foo");
		assertThat(OciRegistryUrl.hostAndPath("oci://https://registry.example.com/foo")).isEqualTo("registry.example.com/foo");
		assertThat(OciRegistryUrl.hostAndPath("oci://registry.example.com/foo")).isEqualTo("registry.example.com/foo");
	}

}
