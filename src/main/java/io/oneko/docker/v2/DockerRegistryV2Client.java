package io.oneko.docker.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.oneko.docker.DockerRegistry;
import io.oneko.docker.v2.model.ListTagsResult;
import io.oneko.docker.v2.model.Repository;
import io.oneko.docker.v2.model.RepositoryList;
import io.oneko.docker.v2.model.manifest.Manifest;
import io.oneko.project.Project;
import io.oneko.project.ProjectVersion;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Accesses the API defined here:
 * https://github.com/moby/moby/issues/9015
 */
public class DockerRegistryV2Client {

	private final DockerRegistry reg;
	private final CloseableHttpClient client;
	private final ObjectMapper objectMapper;

	public DockerRegistryV2Client(DockerRegistry reg, String token, ObjectMapper objectMapper) {
		this.reg = reg;
		this.objectMapper = objectMapper;
		List<Header> defaultHeaders = new ArrayList<>();
		defaultHeaders.add(new BasicHeader("docker-distribution-api-version","registry/2.0" ));
		if (token != null) {
			defaultHeaders.add(new BasicHeader(AuthorizationHeader.KEY, AuthorizationHeader.bearer(token)));
		}
		this.client = HttpClients.custom()
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
				.setDefaultHeaders(defaultHeaders)
				.build();
	}

	public String versionCheck() {
		HttpGet get = new HttpGet(reg.getRegistryUrl() + "/v2");
		try (CloseableHttpResponse response = client.execute(get)) {
			return EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			//TODO: Error handling
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Warn: not working with most docker registries!
	 */
	public List<String> getAllImageNames() {
		//most providers of v2 API won't allow accessing the catalog...
		HttpGet get = new HttpGet(reg.getRegistryUrl() + "/v2/_catalog");
		try (CloseableHttpResponse response = client.execute(get)) {
			RepositoryList repositories = this.objectMapper.readValue(EntityUtils.toString(response.getEntity()), RepositoryList.class);
			return repositories.getRepositories().stream().map(Repository::getName).collect(Collectors.toList());
		} catch (IOException e) {
			//TODO: Error handling
			throw new IllegalStateException(e);
		}
	}

	public List<String> getAllTags(Project<?, ?> project) {
		HttpGet get = new HttpGet(reg.getRegistryUrl() + "/v2/" + project.getImageName() + "/tags/list");
		try (CloseableHttpResponse response = client.execute(get)) {
			ListTagsResult listTagsResult = this.objectMapper.readValue(EntityUtils.toString(response.getEntity()), ListTagsResult.class);
			return listTagsResult.getTags();
		} catch (IOException e) {
			//TODO: Error handling
			throw new IllegalStateException(e);
		}
	}

	public Manifest getManifest(ProjectVersion<?, ?> version) {
		HttpGet get = new HttpGet(reg.getRegistryUrl() + "/v2/" + version.getProject().getImageName() + "/manifests/" + version.getName());
		try (CloseableHttpResponse response = client.execute(get)) {
			Header[] dockerContentDigestHeaders = response.getHeaders("Docker-Content-Digest");
			Manifest manifest = this.objectMapper.readValue(EntityUtils.toString(response.getEntity()), Manifest.class);
			Arrays.stream(dockerContentDigestHeaders).findFirst()
					.map(Header::getValue)
					.ifPresent(manifest::setDockerContentDigest);
			return manifest;
		} catch (IOException e) {
			//TODO: Error handling
			throw new IllegalStateException(e);
		}
	}
}
