package io.oneko.helm.rest;

import java.util.UUID;

import lombok.Data;

@Data
public class HelmRegistryDTO {
	private UUID id;
	private String name;
	private String url;
	private String username;
}
