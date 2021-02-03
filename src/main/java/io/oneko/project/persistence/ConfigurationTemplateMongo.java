package io.oneko.project.persistence;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class ConfigurationTemplateMongo {
	@Id
	private UUID id;

	private String content;
	private String name;
	private String description;
	private String chartName;
	private String chartVersion;
	private UUID helmRegistryId;
}
