package io.oneko.docker.v2;

import feign.Param;
import feign.RequestLine;
import io.oneko.docker.v2.model.ListTagsResult;
import io.oneko.docker.v2.model.manifest.DockerRegistryBlob;
import io.oneko.docker.v2.model.manifest.DockerRegistryManifest;

public interface DockerRegistryAPIV2 {

	@RequestLine("GET /v2/")
	String versionCheck();

	@RequestLine("GET /v2/{imageName}/tags/list")
	ListTagsResult getAllTags(@Param("imageName") String imageName);

	@RequestLine("GET /v2/{imageName}/manifests/{tagName}")
	DockerRegistryManifest getManifest(@Param("imageName") String imageName, @Param("tagName") String tagName);

	@RequestLine("GET /v2/{imageName}/blobs/{algorithm}:{digest}")
	DockerRegistryBlob getBlob(@Param("imageName") String imageName, @Param("algorithm") String algorithm, @Param("digest") String digest);

}
