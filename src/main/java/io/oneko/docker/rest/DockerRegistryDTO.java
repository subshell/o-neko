package io.oneko.docker.rest;

import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DockerRegistryDTO {
	private UUID uuid;
	private String name;
	private String registryUrl;
	private String userName;
	private boolean trustInsecureCertificate;
}
