package io.oneko.docker.v2;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicates;

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
				.retrieve()
				.onStatus(Predicates.or(HttpStatus::is4xxClientError, HttpStatus::is5xxServerError), (s) -> Mono.error(new RuntimeException("Unable to retrieve manifest for version " + version.getName() + " due to error " + s.statusCode().getReasonPhrase())))
				.toEntity(String.class).flatMap(response -> {
					HttpHeaders headers = response.getHeaders();
					String dockerContentDigest = headers.getFirst("Docker-Content-Digest");
					try {
						// the response has the content-type "application/vnd.docker.distribution.manifest.v1+prettyjws"
						Manifest manifest = objectMapper.readValue(response.getBody(), Manifest.class);
						manifest.setDockerContentDigest(dockerContentDigest);
						return Mono.just(manifest);
					} catch (IOException e) {
						return Mono.error(new RuntimeException("Failed to deserialize Manifest from response."));
					}
				});
	}
}