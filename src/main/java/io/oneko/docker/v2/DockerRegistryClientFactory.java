package io.oneko.docker.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.oneko.docker.DockerRegistry;
import io.oneko.docker.DockerRegistryRepository;
import io.oneko.docker.v2.DockerRegistryVersionChecker.BearerAuthRequired;
import io.oneko.docker.v2.DockerRegistryVersionChecker.V2APIUnavailable;
import io.oneko.docker.v2.model.TokenResponse;
import io.oneko.project.Project;
import io.oneko.projectmesh.MeshComponent;
import io.oneko.projectmesh.MeshService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
public class DockerRegistryClientFactory {

	private final ObjectMapper objectMapper;
	private final DockerRegistryVersionChecker dockerRegistryVersionChecker;
	private final DockerRegistryRepository dockerRegistryRepository;
	private final MeshService meshService;

	@Autowired
	DockerRegistryClientFactory(ObjectMapper objectMapper, DockerRegistryVersionChecker dockerRegistryVersionChecker, DockerRegistryRepository dockerRegistryRepository, MeshService meshService) {
		this.objectMapper = objectMapper;
		this.dockerRegistryVersionChecker = dockerRegistryVersionChecker;
		this.dockerRegistryRepository = dockerRegistryRepository;
		this.meshService = meshService;
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
		var dockerRegistryCheckResult = dockerRegistryVersionChecker.checkV2ApiOf(dockerRegistry);
		return buildClientBasedOnApiCheck(dockerRegistryCheckResult, dockerRegistry, "registry:catalog:*");
	}

	public Optional<DockerRegistryV2Client> getDockerRegistryClient(Project<?, ?> project) {
		if (project.isOrphan()) {
			return Optional.empty();
		}
		return dockerRegistryRepository.getById(project.getDockerRegistryId())
				.map(dockerRegistry -> {
					DockerRegistryVersionChecker.DockerRegistryCheckResult dockerRegistryCheckResult = dockerRegistryVersionChecker.checkV2ApiOf(dockerRegistry);
					return this.buildClientBasedOnApiCheck(dockerRegistryCheckResult, dockerRegistry, "repository:" + project.getImageName() + ":pull");
				});
	}

	public Optional<DockerRegistryV2Client> getDockerRegistryClient(MeshComponent<?, ?> component) {
		return getDockerRegistryClient(meshService.getVersionOfComponent(component).getProject());
	}

	/**
	 * Creates a V2 client for the given registry and scope based on the API check result.
	 *
	 * @param checkResult
	 * @param registry     The registry for which a client is needed.
	 * @param desiredScope see <a href="https://docs.docker.com/registry/spec/auth/scope/">docker docs</a> on what to pass in here.
	 * @return
	 */
	private DockerRegistryV2Client buildClientBasedOnApiCheck(DockerRegistryVersionChecker.DockerRegistryCheckResult checkResult, DockerRegistry registry, String desiredScope) {
		if (checkResult == DockerRegistryVersionChecker.DockerRegistryCheckResult.UnsupportedAuthenticationType) {
			throw new IllegalStateException("Docker registry " + registry.getName() + " does not support the V2 API");
		} else if (checkResult == DockerRegistryVersionChecker.DockerRegistryCheckResult.Okay) {
			return new DockerRegistryV2Client(registry, null, objectMapper);
		} else if (checkResult instanceof V2APIUnavailable) {
			throw new IllegalStateException("V2 API of docker registry " + registry.getName() + " is unavailable with status code " + ((V2APIUnavailable) checkResult).getStatusCode());
		} else if (checkResult instanceof BearerAuthRequired) {
			BearerAuthRequired required = (BearerAuthRequired) checkResult;
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
	private TokenResponse requestToken(DockerRegistry registry, BearerAuthRequired required, String scope) {
		HttpClientBuilder builder = HttpClients.custom();
		if (registry.isTrustInsecureCertificate()) {
			builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
		}
		CloseableHttpClient client = builder.build();

		HttpGet request = new HttpGet(required.getRealm() + "?service=" + required.getService() + "&scope=" + scope);
		request.addHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, AuthorizationHeader.basic(registry.getUserName(), registry.getPassword())));

		try (CloseableHttpResponse response = client.execute(request)) {
			return this.objectMapper.readValue(EntityUtils.toString(response.getEntity()), TokenResponse.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
