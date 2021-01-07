package io.oneko.docker.v2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.oneko.docker.DockerRegistry;
import io.oneko.docker.v2.model.ListTagsResult;
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

	private final DockerRegistry reg;
	private final CloseableHttpClient client;
	private final ObjectMapper objectMapper;

	public DockerRegistryV2Client(DockerRegistry reg, String token, ObjectMapper objectMapper) {
		this.reg = reg;
		this.objectMapper = objectMapper;
		List<Header> defaultHeaders = new ArrayList<>();
		defaultHeaders.add(new BasicHeader("Accept", "*/*"));
		if (token != null) {
			defaultHeaders.add(new BasicHeader(AuthorizationHeader.KEY, AuthorizationHeader.bearer(token)));
		}
		this.client = HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom()
						.setCookieSpec(CookieSpecs.STANDARD)
						.build())
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
				.setDefaultHeaders(defaultHeaders)
				.build();
	}

	public String versionCheck() {
		HttpGet get = new HttpGet(reg.getRegistryUrl() + "/v2");
		try (CloseableHttpResponse response = client.execute(get)) {
			return EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			log.warn("Failed to check docker registry version", e);
			throw new IllegalStateException(e);
		}
	}

	public List<String> getAllTags(Project<?, ?> project) {
		HttpGet get = new HttpGet(reg.getRegistryUrl() + "/v2/" + project.getImageName() + "/tags/list");
		try (CloseableHttpResponse response = client.execute(get)) {
			ListTagsResult listTagsResult = this.objectMapper.readValue(EntityUtils.toString(response.getEntity()), ListTagsResult.class);
			return listTagsResult.getTags();
		} catch (IOException e) {
			log.warn("Failed to list all tags for image {}", project.getImageName(), e);
			throw new IllegalStateException(e);
		}
	}

	public Manifest getManifest(ProjectVersion<?, ?> version) {
		HttpGet manifestGet = new HttpGet(reg.getRegistryUrl() + "/v2/" + version.getProject().getImageName() + "/manifests/" + version.getName());
		try (CloseableHttpResponse response = client.execute(manifestGet)) {
			final String responseString = EntityUtils.toString(response.getEntity());
			DockerRegistryManifest registryManifest = this.objectMapper.readValue(responseString, DockerRegistryManifest.class);
			HttpGet blobGet = new HttpGet(reg.getRegistryUrl() + "/v2/" + version.getProject().getImageName() + "/blobs/" + registryManifest.getDigest());
			try (CloseableHttpResponse blobResponse = client.execute(blobGet)) {
				final DockerRegistryBlob dockerRegistryBlob = this.objectMapper.readValue(EntityUtils.toString(blobResponse.getEntity()), DockerRegistryBlob.class);

				return new Manifest(registryManifest.getDigest(), dockerRegistryBlob.getCreated());
			}

		} catch (IOException e) {
			log.warn("Failed to get manifest for project version {} of project {}", version.getName(), version.getProject().getName(), e);
			throw new IllegalStateException(e);
		}
	}

}
