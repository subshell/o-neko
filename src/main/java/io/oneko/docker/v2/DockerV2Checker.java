package io.oneko.docker.v2;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import io.oneko.docker.DockerRegistry;
import io.oneko.security.WebClientBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Checks whether the V2 API is available for a docker registry and whether authentication is required.
 */
@Slf4j
@Component
public class DockerV2Checker {

	public Mono<V2CheckResult> checkV2ApiOf(DockerRegistry registry) {
		WebClient client = WebClientBuilderFactory.create(registry.isTrustInsecureCertificate()).baseUrl(registry.getRegistryUrl() + "/v2/").build();
		return client.get()
				.exchange()
				.flatMap(this::mapResponseToCheckResult);
	}

	private Mono<V2CheckResult> mapResponseToCheckResult(ClientResponse clientResponse) {
			HttpStatus httpStatus = clientResponse.statusCode();
			ClientResponse.Headers headers = clientResponse.headers();
		return clientResponse.bodyToMono(Void.class).map(body -> {
				// the body is not needed here, but it must be consumed
				// see https://github.com/reactor/reactor-netty/issues/778
				if (httpStatus == HttpStatus.OK) {
					return V2CheckResult.V2Okay;
				} else if (httpStatus == HttpStatus.NOT_FOUND) {
					return V2CheckResult.V2NotSupported;
				} else if (httpStatus == HttpStatus.UNAUTHORIZED) {
					//parse authenticate
					List<String> header = headers.header(HttpHeaders.WWW_AUTHENTICATE);
					if (header.size() < 1) {
						return V2CheckResult.V2NotSupported;
					}
					String authenticateHeaderValue = header.get(0);
					String type = StringUtils.substringBefore(authenticateHeaderValue, " realm");
					String realm = StringUtils.substringBetween(authenticateHeaderValue, "realm=\"", "\"");
					String service = StringUtils.substringBetween(authenticateHeaderValue, "service=\"", "\"");
					if (StringUtils.equalsIgnoreCase(type, "Bearer")) {
						return new BearerAuthRequired(realm, service);
					} else {
						log.warn("Docker registry returned unsupported authentication type '{}'", type);
						return V2CheckResult.V2NotSupported;
					}
				} else {
					return new V2Unavailable(httpStatus.value());
				}
			});
	}

	public static class V2CheckResult {
		public static final V2CheckResult V2Okay = new V2CheckResult();
		public static final V2CheckResult V2NotSupported = new V2CheckResult();
		private V2CheckResult() {
		}
	}

	public class V2Unavailable extends V2CheckResult {
		private final int statusCode;

		public V2Unavailable(int statusCode) {
			this.statusCode = statusCode;
		}

		public int getStatusCode() {
			return statusCode;
		}
	}

	public class BearerAuthRequired extends V2CheckResult {
		private final String realm;
		private final String service;

		public BearerAuthRequired(String realm, String service) {
			this.realm = realm;
			this.service = service;
		}

		public String getRealm() {
			return realm;
		}

		public String getService() {
			return service;
		}
	}

}
