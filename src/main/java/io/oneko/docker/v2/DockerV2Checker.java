package io.oneko.docker.v2;

import io.oneko.docker.DockerRegistry;
import lombok.extern.slf4j.Slf4j;
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

import java.io.IOException;

/**
 * Checks whether the V2 API is available for a docker registry and whether authentication is required.
 */
@Slf4j
@Component
public class DockerV2Checker {

	public V2CheckResult checkV2ApiOf(DockerRegistry registry) {
		CloseableHttpClient client = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
		HttpGet request = new HttpGet(registry.getRegistryUrl() + "/v2/");
		try (CloseableHttpResponse response = client.execute(request)) {
			return mapResponseToCheckResult(response);
		} catch (IOException e) {
			e.printStackTrace();
			//TODO: better error handling here
			throw new IllegalStateException(e);
		}
	}

	private V2CheckResult mapResponseToCheckResult(CloseableHttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == org.apache.http.HttpStatus.SC_OK) {
			return V2CheckResult.V2Okay;
		} else if (statusCode == HttpStatus.SC_NOT_FOUND) {
			return V2CheckResult.V2NotSupported;
		} else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
			//parse authenticate
			Header[] authHeader = response.getHeaders(HttpHeaders.WWW_AUTHENTICATE);
			if (authHeader.length < 1) {
				return V2CheckResult.V2NotSupported;
			}
			String authenticateHeaderValue = authHeader[0].getValue();
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
			return new V2Unavailable(statusCode);
		}
	}

	public static class V2CheckResult {
		public static final V2CheckResult V2Okay = new V2CheckResult();
		public static final V2CheckResult V2NotSupported = new V2CheckResult();

		private V2CheckResult() {
		}
	}

	public static class V2Unavailable extends V2CheckResult {
		private final int statusCode;

		public V2Unavailable(int statusCode) {
			this.statusCode = statusCode;
		}

		public int getStatusCode() {
			return statusCode;
		}
	}

	public static class BearerAuthRequired extends V2CheckResult {
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
