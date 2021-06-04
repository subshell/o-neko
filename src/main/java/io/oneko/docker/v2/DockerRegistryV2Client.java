package io.oneko.docker.v2;

import static io.oneko.util.MoreStructuredArguments.*;
import static net.logstash.logback.argument.StructuredArguments.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Feign;
import feign.FeignException;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import io.oneko.docker.DockerRegistry;
import io.oneko.docker.v2.model.manifest.DockerRegistryBlob;
import io.oneko.docker.v2.model.manifest.DockerRegistryManifest;
import io.oneko.docker.v2.model.manifest.Manifest;
import io.oneko.project.Project;
import io.oneko.project.ProjectVersion;
import lombok.extern.slf4j.Slf4j;

/**
 * Accesses the API defined here:
 * https://docs.docker.com/registry/spec/api/
 */
@Slf4j
public class DockerRegistryV2Client {

	private final DockerRegistryAPIV2 feignClient;

	public DockerRegistryV2Client(DockerRegistry registry, String token, ObjectMapper objectMapper) {
		List<Header> defaultHeaders = new ArrayList<>();
		defaultHeaders.add(new BasicHeader("Accept", "*/*"));
		if (token != null) {
			defaultHeaders.add(new BasicHeader(AuthorizationHeader.KEY, AuthorizationHeader.bearer(token)));
		}
		var builder = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom()
						.setCookieSpec(CookieSpecs.STANDARD)
						.build())
				.setDefaultHeaders(defaultHeaders);
		if (registry.isTrustInsecureCertificate()) {
			builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
		}
		final var client = builder.build();
		this.feignClient = Feign.builder()
				.decoder(new JacksonDecoder(objectMapper))
				.encoder(new JacksonEncoder(objectMapper))
				.logger(new Slf4jLogger())
				.client(new ApacheHttpClient(client))
				.target(DockerRegistryAPIV2.class, registry.getRegistryUrl());
	}

	public String versionCheck() {
		try {
			return feignClient.versionCheck();
		} catch (FeignException e) {
			log.warn("failed to check docker registry version", e);
			throw e;
		}
	}

	public List<String> getAllTags(Project<?, ?> project) {
		try {
			return feignClient.getAllTags(project.getImageName()).getTags();
		} catch (FeignException e) {
			log.warn("failed to list all container image tags ({})", kv("image_name", project.getImageName()), e);
			throw e;
		}
	}

	public Manifest getManifest(ProjectVersion<?, ?> version) {
		try {
			final String imageName = version.getProject().getImageName();
			final DockerRegistryManifest dockerRegistryManifest = feignClient.getManifest(imageName, version.getName());
			final DockerRegistryManifest.Digest digest = dockerRegistryManifest.getDigest();
			final DockerRegistryBlob blob = feignClient.getBlob(imageName, digest.getAlgorithm(), digest.getDigest());
			return new Manifest(digest.getFullDigest(), blob.getCreated());
		} catch (FeignException e) {
			log.warn("failed to get manifest for project version ({}, {})", versionKv(version), projectKv(version.getProject()), e);
			throw e;
		}
	}

}
