package io.oneko.templates.rest;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ConfigurationTemplateDTO {
	private UUID id;

	private String content;
	private String name;
	private String description;
}
