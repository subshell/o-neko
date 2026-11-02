package io.oneko.helm.oci;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.oneko.docker.v2.AuthorizationHeader;
import io.oneko.helm.HelmRegistry;
import io.oneko.helm.HelmRegistryException;
import io.oneko.helmapi.model.Chart;
import lombok.extern.slf4j.Slf4j;

/**
 * Talks to the OCI distribution API (<a href="https://docs.docker.com/registry/spec/api/">spec</a>) of a helm OCI
 * registry directly, since the helm CLI itself offers no way to discover charts in an OCI registry (unlike classic
 * repos, which can be searched via their index.yaml).
 */
@Slf4j
public class OciHelmRegistryClient {

	private static final String HELM_CHART_CONFIG_MEDIA_TYPE = "application/vnd.cncf.helm.config.v1+json";
	private static final String MANIFEST_ACCEPT_HEADER = "application/vnd.oci.image.manifest.v1+json, application/vnd.docker.distribution.manifest.v2+json";

	private final String baseUrl;
	private final String repositoryPathPrefix;
	private final String username;
	private final String password;
	private final ObjectMapper objectMapper;
	private final CloseableHttpClient httpClient = HttpClients.createDefault();
	private final Map<String, String> authHeadersByScope = new HashMap<>();
	private String staticAuthHeader;

	public OciHelmRegistryClient(HelmRegistry registry, ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.username = registry.getUsername();
		this.password = registry.getPassword();

		String withoutScheme = StringUtils.removeStart(registry.getUrl(), "oci://");
		int slashIndex = withoutScheme.indexOf('/');
		String host = slashIndex < 0 ? withoutScheme : withoutScheme.substring(0, slashIndex);
		this.repositoryPathPrefix = slashIndex < 0 ? "" : StringUtils.strip(withoutScheme.substring(slashIndex + 1), "/");
		this.baseUrl = determineBaseUrl(host);
	}

	/**
	 * Prefers https, but falls back to plain http for registries that aren't reachable via TLS at all (e.g. local
	 * development registries), rather than failing outright.
	 */
	private String determineBaseUrl(String host) {
		String httpsUrl = "https://" + host;
		if (respondsOverHttp(httpsUrl)) {
			return httpsUrl;
		}
		log.debug("OCI registry {} is not reachable via https, falling back to plain http", host);
		return "http://" + host;
	}

	private boolean respondsOverHttp(String candidateBaseUrl) {
		try (CloseableHttpResponse response = httpClient.execute(new HttpGet(candidateBaseUrl + "/v2/"))) {
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Combines a chart path given relative to this registry's URL (i.e. what a user would type after the configured
	 * registry URL to reference a chart) with this registry's own path prefix into a repository path relative to the
	 * registry host, as used by the rest of this client's methods.
	 */
	public String resolveRepositoryPath(String chartPath) {
		String cleanedChartPath = StringUtils.strip(StringUtils.defaultString(chartPath), "/");
		if (repositoryPathPrefix.isEmpty()) {
			return cleanedChartPath;
		}
		return cleanedChartPath.isEmpty() ? repositoryPathPrefix : repositoryPathPrefix + "/" + cleanedChartPath;
	}

	/**
	 * Lists all repositories below this registry's configured path prefix via the (optional) catalog endpoint.
	 * Returns an empty list, rather than failing, if the registry does not support that endpoint (e.g. Docker Hub,
	 * GHCR and ECR do not implement it) - use {@link #resolveRepositoryPath(String)} with a known chart path instead
	 * in that case.
	 */
	public List<String> listChartRepositories() {
		Optional<JsonNode> catalog = tryGet("/v2/_catalog?n=10000", "registry:catalog:*");
		if (catalog.isEmpty()) {
			return List.of();
		}
		List<String> repositories = new ArrayList<>();
		catalog.get().path("repositories").forEach(node -> repositories.add(node.asText()));

		if (repositoryPathPrefix.isEmpty()) {
			return repositories;
		}
		return repositories.stream()
				.filter(repo -> repo.equals(repositoryPathPrefix) || repo.startsWith(repositoryPathPrefix + "/"))
				.collect(Collectors.toList());
	}

	/**
	 * Lists all helm chart versions found in the given repository (a path relative to the registry host, as returned
	 * by {@link #listChartRepositories()} or {@link #resolveRepositoryPath(String)}). Tags that turn out not to be
	 * helm charts (e.g. container images sharing the same repository) are silently skipped.
	 */
	public List<Chart> listChartsIn(String repositoryPath) throws HelmRegistryException {
		String chartReference = "oci://" + StringUtils.removeStart(baseUrl, "https://") + "/" + repositoryPath;
		List<Chart> charts = new ArrayList<>();
		for (String tag : getTags(repositoryPath)) {
			try {
				getChartConfig(repositoryPath, tag)
						.ifPresent(config -> charts.add(new Chart(chartReference, tag, config.getAppVersion(), config.getDescription())));
			} catch (HelmRegistryException e) {
				log.warn("failed to inspect OCI artifact {}:{}, skipping it during helm chart discovery", repositoryPath, tag, e);
			}
		}
		return charts;
	}

	private List<String> getTags(String repositoryPath) throws HelmRegistryException {
		JsonNode result = get("/v2/" + repositoryPath + "/tags/list", "repository:" + repositoryPath + ":pull");
		List<String> tags = new ArrayList<>();
		result.path("tags").forEach(node -> tags.add(node.asText()));
		return tags;
	}

	private Optional<HelmOciChartConfig> getChartConfig(String repositoryPath, String tag) throws HelmRegistryException {
		String scope = "repository:" + repositoryPath + ":pull";
		JsonNode manifest = get("/v2/" + repositoryPath + "/manifests/" + tag, scope, MANIFEST_ACCEPT_HEADER);
		JsonNode config = manifest.path("config");
		if (!HELM_CHART_CONFIG_MEDIA_TYPE.equals(config.path("mediaType").asText())) {
			log.debug("skipping {}:{} during helm chart discovery, its manifest config media type is '{}' instead of '{}'",
					repositoryPath, tag, config.path("mediaType").asText(), HELM_CHART_CONFIG_MEDIA_TYPE);
			return Optional.empty();
		}
		JsonNode configBlob = get("/v2/" + repositoryPath + "/blobs/" + config.path("digest").asText(), scope);
		try {
			return Optional.of(objectMapper.treeToValue(configBlob, HelmOciChartConfig.class));
		} catch (IOException e) {
			throw new HelmRegistryException("failed to parse helm chart config of " + repositoryPath + ":" + tag, e);
		}
	}

	private Optional<JsonNode> tryGet(String path, String scope) {
		try {
			return Optional.of(get(path, scope));
		} catch (HelmRegistryException e) {
			log.debug("OCI registry {} does not support {}: {}", baseUrl, path, e.getMessage());
			return Optional.empty();
		}
	}

	private JsonNode get(String path, String scope) throws HelmRegistryException {
		return get(path, scope, "application/json");
	}

	private JsonNode get(String path, String scope, String accept) throws HelmRegistryException {
		try {
			String authHeader = staticAuthHeader != null ? staticAuthHeader : authHeadersByScope.get(scope);
			try (CloseableHttpResponse response = executeGet(path, accept, authHeader)) {
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
					String newAuthHeader = resolveAuthFromChallenge(response, scope);
					if (newAuthHeader == null) {
						throw toException(path, response);
					}
					try (CloseableHttpResponse retry = executeGet(path, accept, newAuthHeader)) {
						return parseJsonOrThrow(path, retry);
					}
				}
				return parseJsonOrThrow(path, response);
			}
		} catch (IOException e) {
			throw new HelmRegistryException("failed to call OCI registry endpoint " + baseUrl + path + ": " + e.getMessage(), e);
		}
	}

	private CloseableHttpResponse executeGet(String path, String accept, String authorizationHeaderValue) throws IOException {
		HttpGet request = new HttpGet(baseUrl + path);
		request.addHeader(HttpHeaders.ACCEPT, accept);
		if (authorizationHeaderValue != null) {
			request.addHeader(AuthorizationHeader.KEY, authorizationHeaderValue);
		}
		return httpClient.execute(request);
	}

	private JsonNode parseJsonOrThrow(String path, CloseableHttpResponse response) throws IOException, HelmRegistryException {
		String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
		int status = response.getStatusLine().getStatusCode();
		if (status < 200 || status >= 300) {
			throw new HelmRegistryException("OCI registry endpoint " + baseUrl + path + " returned status " + status + ": " + body);
		}
		return objectMapper.readTree(body);
	}

	private HelmRegistryException toException(String path, CloseableHttpResponse response) {
		return new HelmRegistryException("OCI registry endpoint " + baseUrl + path + " requires authentication we could not satisfy (status "
				+ response.getStatusLine().getStatusCode() + ")");
	}

	/**
	 * Resolves an Authorization header value to retry a request with, based on the registry's WWW-Authenticate
	 * challenge. Supports the token based bearer auth flow described in
	 * <a href="https://docs.docker.com/registry/spec/auth/token/">the docker token authentication spec</a> (used by
	 * most hosted registries), as well as plain Basic auth (used e.g. by a bare, htpasswd-protected
	 * <a href="https://hub.docker.com/_/registry">registry:2</a> container, as is common for local development).
	 */
	private String resolveAuthFromChallenge(CloseableHttpResponse unauthorizedResponse, String scope) throws IOException {
		Header[] authHeaders = unauthorizedResponse.getHeaders(HttpHeaders.WWW_AUTHENTICATE);
		if (authHeaders.length == 0) {
			return null;
		}
		String headerValue = authHeaders[0].getValue();

		if (StringUtils.startsWithIgnoreCase(headerValue, "Basic")) {
			if (StringUtils.isBlank(username)) {
				return null;
			}
			staticAuthHeader = AuthorizationHeader.basic(username, password);
			return staticAuthHeader;
		}

		if (!StringUtils.startsWithIgnoreCase(headerValue, "Bearer")) {
			return null;
		}
		String realm = StringUtils.substringBetween(headerValue, "realm=\"", "\"");
		String service = StringUtils.substringBetween(headerValue, "service=\"", "\"");
		if (realm == null) {
			return null;
		}

		StringBuilder tokenUrl = new StringBuilder(realm).append("?scope=").append(scope);
		if (service != null) {
			tokenUrl.append("&service=").append(service);
		}
		HttpGet tokenRequest = new HttpGet(tokenUrl.toString());
		if (StringUtils.isNotBlank(username)) {
			tokenRequest.addHeader(AuthorizationHeader.KEY, AuthorizationHeader.basic(username, password));
		}
		try (CloseableHttpResponse tokenResponse = httpClient.execute(tokenRequest)) {
			if (tokenResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				return null;
			}
			JsonNode json = objectMapper.readTree(EntityUtils.toString(tokenResponse.getEntity(), StandardCharsets.UTF_8));
			JsonNode tokenNode = json.has("token") ? json.get("token") : json.get("access_token");
			if (tokenNode == null) {
				return null;
			}
			String authHeader = AuthorizationHeader.bearer(tokenNode.asText());
			authHeadersByScope.put(scope, authHeader);
			return authHeader;
		}
	}

}
