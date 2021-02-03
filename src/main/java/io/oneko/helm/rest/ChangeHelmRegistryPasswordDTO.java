package io.oneko.helm.rest;

import java.util.UUID;

import lombok.Data;

@Data
public class ChangeHelmRegistryPasswordDTO {
	private UUID uuid;
	private String username;
	private String password;
}
