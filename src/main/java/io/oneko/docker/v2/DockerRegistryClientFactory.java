package io.oneko.docker.v2;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import io.oneko.docker.DockerRegistry;
import io.oneko.docker.DockerRegistryRepository;
import io.oneko.docker.v2.DockerRegistryVersionChecker.BearerAuthRequired;
import io.oneko.docker.v2.DockerRegistryVersionChecker.V2APIUnavailable;
import io.oneko.docker.v2.metrics.DockerRegistryClientMetrics;
import io.oneko.docker.v2.model.TokenResponse;
import io.oneko.metrics.MetricNameBuilder;
import io.oneko.project.Project;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DockerRegistryClientFactory {

	private final ObjectMapper objectMapper;
	private final DockerRegistryVersionChecker dockerRegistryVersionChecker;
	private final DockerRegistryRepository dockerRegistryRepository;
	private final DockerRegistryClientMetrics dockerRegistryClientMetrics;
	private final Cache<UUID, DockerRegistryV2Client> projectToDockerRegistryClientCache = Caffeine.newBuilder()
			.expireAfterWrite(Duration.ofMinutes(5))
			.recordStats()
			.build();

	private final Timer clientBuildTimerCatalog;

	private final Timer clientBuildTimerRepository;

	private final Timer tokenRequestTimer;


	@Autowired
	DockerRegistryClientFactory(ObjectMapper objectMapper,
															DockerRegistryVersionChecker dockerRegistryVersionChecker,
															DockerRegistryRepository dockerRegistryRepository,
															MeterRegistry meterRegistry,
															DockerRegistryClientMetrics dockerRegistryClientMetrics) {

		this.objectMapper = objectMapper;
		this.dockerRegistryVersionChecker = dockerRegistryVersionChecker;
		this.dockerRegistryRepository = dockerRegistryRepository;
		this.dockerRegistryClientMetrics = dockerRegistryClientMetrics;

		CaffeineCacheMetrics.monitor(meterRegistry, projectToDockerRegistryClientCache, "dockerRegistryClientCache");
		clientBuildTimerCatalog = Timer.builder(new MetricNameBuilder().durationOf("docker.registry.client.build").build())
				.description("the time it takes to build a container registry client")
				.tag("scope", "catalog")
				.publishPercentileHistogram()
				.register(meterRegistry);
		clientBuildTimerRepository = Timer.builder(new MetricNameBuilder().durationOf("docker.registry.client.build").build())
				.description("the time it takes to build a container registry client")
				.tag("scope", "repository")
				.publishPercentileHistogram()
				.register(meterRegistry);
		tokenRequestTimer = Timer.builder(new MetricNameBuilder().durationOf("docker.registry.tokenrequest").build())
				.description("the time it takes to request the token for interacting with the registry")
				.publishPercentileHistogram()
				.register(meterRegistry);
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
		return clientBuildTimerCatalog.record(() -> {
			var dockerRegistryCheckResult = dockerRegistryVersionChecker.checkV2ApiOf(dockerRegistry);
			return buildClientBasedOnApiCheck(dockerRegistryCheckResult, dockerRegistry, "registry:catalog:*");
		});
	}

	public Optional<DockerRegistryV2Client> getDockerRegistryClient(Project<?, ?> project) {
		if (project.isOrphan()) {
			return Optional.empty();
		}
		return Optional.ofNullable(projectToDockerRegistryClientCache.get(project.getId(), uuid -> buildDockerRegistryClientForProject(project).orElse(null)));
	}

	private Optional<DockerRegistryV2Client> buildDockerRegistryClientForProject(Project<?, ?> project) {
		return clientBuildTimerRepository.record(() -> dockerRegistryRepository.getById(project.getDockerRegistryId())
				.map(dockerRegistry -> {
					DockerRegistryVersionChecker.DockerRegistryCheckResult dockerRegistryCheckResult = dockerRegistryVersionChecker.checkV2ApiOf(dockerRegistry);
					return this.buildClientBasedOnApiCheck(dockerRegistryCheckResult, dockerRegistry, "repository:" + project.getImageName() + ":pull");
				})
		);
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
			return new DockerRegistryV2Client(registry, null, objectMapper, dockerRegistryClientMetrics.getMeters(registry));
		} else if (checkResult instanceof V2APIUnavailable) {
			throw new IllegalStateException("V2 API of docker registry " + registry.getName() + " is unavailable with status code " + ((V2APIUnavailable) checkResult).getStatusCode());
		} else if (checkResult instanceof BearerAuthRequired) {
			BearerAuthRequired required = (BearerAuthRequired) checkResult;
			TokenResponse tokenResponse = requestToken(registry, required, desiredScope);
			return new DockerRegistryV2Client(registry, tokenResponse.getToken(), objectMapper, dockerRegistryClientMetrics.getMeters(registry));
		} else {
			throw new IllegalStateException("CheckResult" + checkResult + " not supporter by docker client factory");
		}
	}

	/**
	 * Request a token for bearer authentication to be used on later requests. The request to get a token however uses
	 * basic authentication with the users credentials.
	 */
	private TokenResponse requestToken(DockerRegistry registry, BearerAuthRequired required, String scope) {
		final Timer.Sample sample = Timer.start();
		HttpClientBuilder builder = HttpClients.custom();
		if (registry.isTrustInsecureCertificate()) {
			builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
		}
		CloseableHttpClient client = builder.build();

		HttpGet request = new HttpGet(required.getRealm() + "?service=" + required.getService() + "&scope=" + scope);
		request.addHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, AuthorizationHeader.basic(registry.getUserName(), registry.getPassword())));

		try (CloseableHttpResponse response = client.execute(request)) {
			final TokenResponse tokenResponse = this.objectMapper.readValue(EntityUtils.toString(response.getEntity()), TokenResponse.class);
			sample.stop(tokenRequestTimer);
			return tokenResponse;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
