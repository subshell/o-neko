package io.oneko.docker.rest;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@Data
public class DockerRegistryDTO {
	private UUID uuid;
	private String name;
	private String registryUrl;
	private String userName;
	private boolean trustInsecureCertificate;
}
