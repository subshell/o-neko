package io.oneko.helm.rest;

import lombok.Data;

@Data
public class CreateHelmRegistryDTO extends HelmRegistryDTO {
	private String password;
}
