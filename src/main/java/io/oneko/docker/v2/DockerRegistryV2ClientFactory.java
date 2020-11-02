package io.oneko.docker.v2;

import io.oneko.docker.DockerRegistryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.oneko.docker.DockerRegistry;
import io.oneko.docker.v2.model.TokenResponse;
import io.oneko.project.Project;
import io.oneko.projectmesh.MeshComponent;
import io.oneko.security.WebClientBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@Slf4j
public class DockerRegistryV2ClientFactory {

	private final ObjectMapper objectMapper;
	private final DockerV2Checker dockerV2Checker;
	private final DockerRegistryRepository dockerRegistryRepository;

	@Autowired
	DockerRegistryV2ClientFactory(ObjectMapper objectMapper, DockerV2Checker dockerV2Checker, DockerRegistryRepository dockerRegistryRepository) {
		this.objectMapper = objectMapper;
		this.dockerV2Checker = dockerV2Checker;
		this.dockerRegistryRepository = dockerRegistryRepository;
	}

	/**
	 * Checks whether the V2 API for this docker registry is available and access using the credentials is possible.
	 *
	 * @param dockerRegistry The registry to check
	 * @return The returned Mono always contains true or emits an error with a readable message.
	 */
	public String checkRegistryAvailability(DockerRegistry dockerRegistry) {
		DockerRegistryV2Client dockerRegistryClient = getDockerRegistryClient(dockerRegistry);
		return dockerRegistryClient.versionCheck();
	}

	public DockerRegistryV2Client getDockerRegistryClient(DockerRegistry dockerRegistry) {
		DockerV2Checker.V2CheckResult v2CheckResult = dockerV2Checker.checkV2ApiOf(dockerRegistry);
		return buildClientBasedOnApiCheck(v2CheckResult, dockerRegistry, "registry:catalog:*");
	}

	public Optional<DockerRegistryV2Client> getDockerRegistryClient(Project<?, ?> project) {
		if (project.isOrphan()) {
			return Optional.empty();
		}
		return dockerRegistryRepository.getById(project.getDockerRegistryId())
				.map(dockerRegistry -> {
					DockerV2Checker.V2CheckResult v2CheckResult = dockerV2Checker.checkV2ApiOf(dockerRegistry);
					return this.buildClientBasedOnApiCheck(v2CheckResult, dockerRegistry, "repository:" + project.getImageName() + ":pull");
				});
	}

	public Optional<DockerRegistryV2Client> getDockerRegistryClient(MeshComponent<?, ?> component) {
		return getDockerRegistryClient(component.getProject());
	}

	/**
	 * Creates a V2 client for the given registry and scope based on the API check result.
	 *
	 * @param checkResult
	 * @param registry     The registry for which a client is needed.
	 * @param desiredScope see <a href="https://docs.docker.com/registry/spec/auth/scope/">docker docs</a> on what to pass in here.
	 * @return
	 */
	private DockerRegistryV2Client buildClientBasedOnApiCheck(DockerV2Checker.V2CheckResult checkResult, DockerRegistry registry, String desiredScope) {
		if (checkResult == DockerV2Checker.V2CheckResult.V2NotSupported) {
			throw new IllegalStateException("DockerRegistry " + registry.getName() + " is not supporting the v2 API");
		} else if (checkResult == DockerV2Checker.V2CheckResult.V2Okay) {
			return new DockerRegistryV2Client(registry, null, objectMapper);
		} else if (checkResult instanceof DockerV2Checker.V2Unavailable) {
			throw new IllegalStateException("V2 API of DockerRegistry " + registry.getName() + " is unavailable with status code " + ((DockerV2Checker.V2Unavailable) checkResult).getStatusCode());
		} else if (checkResult instanceof DockerV2Checker.BearerAuthRequired) {
			DockerV2Checker.BearerAuthRequired required = (DockerV2Checker.BearerAuthRequired) checkResult;
			TokenResponse tokenResponse = requestToken(registry, required, desiredScope);
			return new DockerRegistryV2Client(registry, tokenResponse.getToken(), objectMapper);
		} else {
			throw new IllegalStateException("CheckResult" + checkResult + " not supporter by docker client factory");
		}
	}

	/**
	 * Request a token for bearer authentication to be used on later requests. The request to get a token however uses
	 * basic authentication with the users credentials.
	 */
	private TokenResponse requestToken(DockerRegistry registry, DockerV2Checker.BearerAuthRequired required, String scope) {
		WebClient client = WebClientBuilderFactory.create(registry.isTrustInsecureCertificate())
				.baseUrl(registry.getRegistryUrl())
				.defaultHeader(AuthorizationHeader.KEY, AuthorizationHeader.basic(registry.getUserName(), registry.getPassword()))
				.build();

		return client.get()
				.uri(required.getRealm() + "?service=" + required.getService() + "&scope=" + scope)
				.retrieve()
				.onStatus(HttpStatus::is4xxClientError, this::getErrorMessage)
				.onStatus(HttpStatus::is5xxServerError, this::getErrorMessage)
				.bodyToMono(TokenResponse.class);
	}

	private Mono<RuntimeException> getErrorMessage(ClientResponse response) {
		return response.bodyToMono(String.class)
				.flatMap(message -> Mono.error(new RuntimeException(message)));
	}

}
