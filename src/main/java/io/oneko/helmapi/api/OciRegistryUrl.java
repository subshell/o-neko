package io.oneko.helmapi.api;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import lombok.extern.slf4j.Slf4j;

/**
 * Parses an OCI registry URL as stored on a helm registry (e.g. {@code oci://host/path}, optionally with an explicit
 * {@code oci://http://host/path} or {@code oci://https://host/path} scheme) into the bare host, the repository path
 * prefix below it, and whether the registry should be talked to via plain HTTP.
 * <p>
 * If no scheme is given explicitly, the registry is probed (https first, falling back to http) rather than failing
 * outright, to support registries that aren't reachable via TLS at all (e.g. local development registries).
 */
@Slf4j
public record OciRegistryUrl(String host, String repositoryPathPrefix, boolean plainHttp) {

	private static final String OCI_SCHEME = "oci://";
	private static final String HTTP_SCHEME = "http://";
	private static final String HTTPS_SCHEME = "https://";

	public static OciRegistryUrl resolve(String url) {
		Parsed parsed = parse(url);
		boolean plainHttp = parsed.explicitPlainHttp != null ? parsed.explicitPlainHttp : !respondsOverHttps(parsed.host);
		return new OciRegistryUrl(parsed.host, parsed.repositoryPathPrefix, plainHttp);
	}

	/**
	 * Strips the {@code oci://} scheme (and, if present, an explicit {@code http://}/{@code https://} scheme below
	 * it) without probing the registry. Use this where only the bare host/path is needed and plain-http-ness is
	 * irrelevant, to avoid an unnecessary network call.
	 */
	public static String hostAndPath(String url) {
		Parsed parsed = parse(url);
		return parsed.repositoryPathPrefix.isEmpty() ? parsed.host : parsed.host + "/" + parsed.repositoryPathPrefix;
	}

	public String getHostAndPath() {
		return repositoryPathPrefix.isEmpty() ? host : host + "/" + repositoryPathPrefix;
	}

	private static Parsed parse(String url) {
		String withoutOciScheme = StringUtils.removeStart(StringUtils.defaultString(url), OCI_SCHEME);

		Boolean explicitPlainHttp = null;
		if (StringUtils.startsWithIgnoreCase(withoutOciScheme, HTTP_SCHEME)) {
			explicitPlainHttp = true;
			withoutOciScheme = withoutOciScheme.substring(HTTP_SCHEME.length());
		} else if (StringUtils.startsWithIgnoreCase(withoutOciScheme, HTTPS_SCHEME)) {
			explicitPlainHttp = false;
			withoutOciScheme = withoutOciScheme.substring(HTTPS_SCHEME.length());
		}

		int slashIndex = withoutOciScheme.indexOf('/');
		String host = slashIndex < 0 ? withoutOciScheme : withoutOciScheme.substring(0, slashIndex);
		String repositoryPathPrefix = slashIndex < 0 ? "" : StringUtils.strip(withoutOciScheme.substring(slashIndex + 1), "/");

		return new Parsed(host, repositoryPathPrefix, explicitPlainHttp);
	}

	private static boolean respondsOverHttps(String host) {
		String httpsUrl = "https://" + host + "/v2/";
		try (CloseableHttpClient httpClient = HttpClients.createDefault();
			CloseableHttpResponse ignored = httpClient.execute(new HttpGet(httpsUrl))) {
			return true;
		} catch (IOException e) {
			log.debug("OCI registry {} is not reachable via https, falling back to plain http", host);
			return false;
		}
	}

	private record Parsed(String host, String repositoryPathPrefix, Boolean explicitPlainHttp) {

	}
}
