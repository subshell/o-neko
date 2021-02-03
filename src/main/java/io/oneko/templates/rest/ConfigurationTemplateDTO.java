package io.oneko.templates.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ConfigurationTemplateDTO {
	private UUID id;

	private String content;
	private String name;
	private String description;
	private String chartName;
	private String chartVersion;
	private UUID helmRegistryId;
}
