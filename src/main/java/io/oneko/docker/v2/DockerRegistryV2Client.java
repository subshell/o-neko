package io.oneko.docker.v2;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.oneko.docker.DockerRegistry;
import io.oneko.docker.v2.model.ListTagsResult;
import io.oneko.docker.v2.model.Repository;
import io.oneko.docker.v2.model.RepositoryList;
import io.oneko.docker.v2.model.manifest.Manifest;
import io.oneko.project.Project;
import io.oneko.project.ProjectVersion;
import io.oneko.security.WebClientBuilderFactory;
import reactor.core.publisher.Mono;

/**
 * Accesses the API defined here:
 * https://github.com/moby/moby/issues/9015
 */
public class DockerRegistryV2Client {

	private final WebClient client;
	private final ObjectMapper objectMapper;

	public DockerRegistryV2Client(DockerRegistry reg, String token, ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;

		WebClient.Builder builder = WebClientBuilderFactory.create(reg.isTrustInsecureCertificate())
				.baseUrl(reg.getRegistryUrl())
				.defaultHeader("docker-distribution-api-version", "registry/2.0");
		if (token != null) {
			builder = builder.defaultHeader(AuthorizationHeader.KEY, AuthorizationHeader.bearer(token));
		}
		client = builder.build();
	}

	public Mono<String> versionCheck() {
		return client.get().uri("/v2")
				.retrieve()
				.bodyToMono(String.class);
	}

	/**
	 * Warn: not working with most docker registries!
	 */
	public Mono<List<String>> getAllImageNames() {
		//most providers of v2 API won't allow accessing the catalog...
		return client.get().uri("/v2/_catalog")
				.retrieve()
				.bodyToMono(RepositoryList.class)
				.map(RepositoryList::getRepositories)
				.map(repositories -> repositories.stream().map(Repository::getName).collect(Collectors.toList()));
	}

	public Mono<List<String>> getAllTags(Project project) {
		return client
				.get()
				.uri("/v2/" + project.getImageName() + "/tags/list")
				.retrieve()
				.bodyToMono(ListTagsResult.class)
				.map(ListTagsResult::getTags);
	}

	public Mono<Manifest> getManifest(ProjectVersion version) {
		return client
				.get()
				.uri("/v2/" + version.getProject().getImageName() + "/manifests/" + version.getName())
				.exchange()
				.flatMap(response -> response.bodyToMono(String.class).flatMap(body -> {
					// the body is not needed here, but it must be consumed
					// see https://github.com/reactor/reactor-netty/issues/778
					if (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
						if (response.statusCode() == HttpStatus.NOT_FOUND) {
							return Mono.error(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Unable to retrieve manifest for version " + version.getName() + "."));
						} else {
							return Mono.error(new RuntimeException("Unable to retrieve manifest for version " + version.getName() + " due to error " + response.statusCode().getReasonPhrase()));
						}
					} else {
						return Mono.just(response);
					}
				}))
				.flatMap(clientResponse -> clientResponse.toEntity(String.class))
				.map(response -> {
					final HttpHeaders headers = response.getHeaders();
					String dockerContentDigest = headers.getFirst("Docker-Content-Digest");
					try {
						// the response has the content-type "application/vnd.docker.distribution.manifest.v1+prettyjws"
						String body = response.getBody();
						Manifest manifest = objectMapper.readValue(body, Manifest.class);
						manifest.setDockerContentDigest(dockerContentDigest);
						return manifest;
					} catch (IOException e) {
						throw new RuntimeException("Failed to deserialize Manifest from response.");
					}
				});

	}
}
