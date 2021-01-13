package io.oneko.docker.v2.model.manifest;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerRegistryBlob {

	private Instant created;

}
