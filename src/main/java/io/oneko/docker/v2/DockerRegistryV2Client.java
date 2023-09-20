package io.oneko.docker.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import feign.Feign;
import feign.FeignException;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import io.micrometer.core.instrument.Timer;
import io.oneko.docker.DockerRegistry;
import io.oneko.docker.v2.metrics.MetersPerRegistry;
import io.oneko.docker.v2.model.manifest.DockerRegistryBlob;
import io.oneko.docker.v2.model.manifest.DockerRegistryManifest;
import io.oneko.docker.v2.model.manifest.Manifest;
import io.oneko.project.Project;
import io.oneko.project.ProjectVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.oneko.util.MoreStructuredArguments.projectKv;
import static io.oneko.util.MoreStructuredArguments.versionKv;
import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * Accesses the API defined here:
 * https://docs.docker.com/registry/spec/api/
 */
@Slf4j
public class DockerRegistryV2Client {

	private final DockerRegistryAPIV2 feignClient;
	private final MetersPerRegistry meters;

	public DockerRegistryV2Client(DockerRegistry registry,
								  String token,
								  ObjectMapper objectMapper,
								  MetersPerRegistry meters) {
		this.meters = meters;
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
		final Timer.Sample sample = Timer.start();
		try {
			final String result = feignClient.versionCheck();
			sample.stop(meters.getVersionCheckTimerOk());
			return result;
		} catch (FeignException e) {
			sample.stop(meters.getVersionCheckTimerError());
			log.warn("failed to check docker registry version", e);
			throw e;
		}
	}

	public List<String> getAllTags(Project<?, ?> project) {
		final Timer.Sample sample = Timer.start();
		try {
			final List<String> result = feignClient.getAllTags(project.getImageName()).getTags();
			sample.stop(meters.getListAllTagsTimerOk());
			return result;
		} catch (FeignException e) {
			sample.stop(meters.getListAllTagsTimerError());
			log.warn("failed to list all container image tags ({})", kv("image_name", project.getImageName()), e);
			throw e;
		}
	}

	public Manifest getManifest(ProjectVersion<?, ?> version) {
		final Timer.Sample sample = Timer.start();
		try {
			final String imageName = version.getProject().getImageName();
			final DockerRegistryManifest dockerRegistryManifest = feignClient.getManifest(imageName, version.getName());

			Manifest result = null;
			if (dockerRegistryManifest.isManifestList()) {
				result = generateManifestFromManifestList(imageName, dockerRegistryManifest);
			} else {
				final DockerRegistryManifest.Digest digest = dockerRegistryManifest.getDigest();
				final DockerRegistryBlob blob = feignClient.getBlob(imageName, digest.getAlgorithm(), digest.getDigest());
				result = new Manifest(digest.getFullDigest(), blob.getCreated());
			}
			sample.stop(meters.getGetManifestTimerOk());
			return result;
		} catch (FeignException e) {
			sample.stop(meters.getGetManifestTimerError());
			log.warn("failed to get manifest for project version ({}, {})", versionKv(version), projectKv(version.getProject()), e);
			throw e;
		}
	}

	/*
	 * This method generates a "pseudo-manifest" for an image that is actually a manifest list (e.g. multiplatform image).
	 * It gets the actual manifests and then the blogs. Then it takes the latest modification date found in the manifests
	 * and creates a hash of all manifest hashes.
	 */
	public Manifest generateManifestFromManifestList(String imageName, DockerRegistryManifest manifestList) {
		return manifestList.getManifests()
			.stream()
			.map(m -> feignClient.getManifest(imageName, m.getDigest().getFullDigest()))
			.map(m -> {
				if (m.isManifestList()) {
					throw new IllegalStateException("received manifest list after getting manifest from manifest list");
				}
				DockerRegistryBlob blob = feignClient.getBlob(imageName, m.getDigest().getAlgorithm(), m.getDigest().getDigest());
				return new Manifest(m.getDigest().getFullDigest(), blob.getCreated());
			}).reduce((m1, m2) -> {
				String hash = "sha512:" + Hashing.sha512().hashString(m1.getDockerContentDigest() + m2.getDockerContentDigest(), StandardCharsets.UTF_8).toString();
				Instant date = m1.getImageUpdatedDate().map(d -> {
					if (m2.getImageUpdatedDate().isPresent() && d.isBefore(m2.getImageUpdatedDate().get())) {
						return m2.getImageUpdatedDate().get();
					}
					return d;
				}).orElse(Instant.MIN);
				return new Manifest(hash, date);
			}).orElseThrow();
	}

}
