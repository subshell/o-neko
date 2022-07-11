package io.oneko.docker.v2;

import static io.oneko.util.MoreStructuredArguments.*;
import static net.logstash.logback.argument.StructuredArguments.*;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.oneko.docker.DockerRegistry;
import io.oneko.metrics.MetricNameBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Checks whether the V2 API is available for a docker registry and whether authentication is required.
 */
@Slf4j
@Component
public class DockerRegistryVersionChecker {
	private final Timer versionCheckTimerSuccess;
	private final Timer versionCheckTimerError;

	public DockerRegistryVersionChecker(MeterRegistry meterRegistry) {
		versionCheckTimerSuccess = Timer.builder(new MetricNameBuilder().durationOf("docker.registry.versioncheck").build())
				.description("time it takes to check whether the v2 api of a container registry is available")
				.publishPercentileHistogram()
				.tag("success", "true")
				.register(meterRegistry);
		versionCheckTimerError = Timer.builder(new MetricNameBuilder().durationOf("docker.registry.versioncheck").build())
				.description("time it takes to check whether the v2 api of a container registry is available")
				.publishPercentileHistogram()
				.tag("success", "false")
				.register(meterRegistry);
	}

	public DockerRegistryCheckResult checkV2ApiOf(DockerRegistry registry) {
		final Timer.Sample sample = Timer.start();
		final var builder = HttpClients.custom();
		if (registry.isTrustInsecureCertificate()) {
			builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
		}
		CloseableHttpClient client = builder.build();
		HttpGet request = new HttpGet(registry.getRegistryUrl() + "/v2/");
		try (CloseableHttpResponse response = client.execute(request)) {
			final var result = mapResponseToCheckResult(response);
			sample.stop(versionCheckTimerSuccess);
			return result;
		} catch (IOException e) {
			sample.stop(versionCheckTimerError);
			log.error("failed to check the docker V2 API ({})", containerRegistryKv(registry), e);
			throw new IllegalStateException(e);
		}
	}

	private DockerRegistryCheckResult mapResponseToCheckResult(CloseableHttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == HttpStatus.SC_OK) {
			return DockerRegistryCheckResult.Okay;
		} else if (statusCode == HttpStatus.SC_NOT_FOUND) {
			return new V2APIUnavailable(statusCode);
		} else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
			//parse authenticate
			Header[] authHeader = response.getHeaders(HttpHeaders.WWW_AUTHENTICATE);
			if (authHeader.length < 1) {
				return DockerRegistryCheckResult.UnsupportedAuthenticationType;
			}
			String authenticateHeaderValue = authHeader[0].getValue();
			String type = StringUtils.substringBefore(authenticateHeaderValue, " realm");
			String realm = StringUtils.substringBetween(authenticateHeaderValue, "realm=\"", "\"");
			String service = StringUtils.substringBetween(authenticateHeaderValue, "service=\"", "\"");
			if (StringUtils.equalsIgnoreCase(type, "Bearer")) {
				return new BearerAuthRequired(realm, service);
			} else {
				log.warn("container registry returned unsupported authentication type ({})", v("authentication_type", type));
				return DockerRegistryCheckResult.UnsupportedAuthenticationType;
			}
		} else {
			return new V2APIUnavailable(statusCode);
		}
	}

	public static class DockerRegistryCheckResult {
		public static final DockerRegistryCheckResult Okay = new DockerRegistryCheckResult();
		public static final DockerRegistryCheckResult UnsupportedAuthenticationType = new DockerRegistryCheckResult();

		private DockerRegistryCheckResult() {
		}
	}

	@Getter
	public static class V2APIUnavailable extends DockerRegistryCheckResult {
		private final int statusCode;

		public V2APIUnavailable(int statusCode) {
			this.statusCode = statusCode;
		}
	}

	@Getter
	public static class BearerAuthRequired extends DockerRegistryCheckResult {
		private final String realm;
		private final String service;

		public BearerAuthRequired(String realm, String service) {
			this.realm = realm;
			this.service = service;
		}
	}

}
