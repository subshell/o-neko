package io.oneko.projectmesh.persistence;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.project.persistence.ConfigurationTemplateMongo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class MeshComponentMongo {
	@Id
	private UUID id;
	private String name;
	private UUID projectId;
	private UUID projectVersionId;
	private String dockerContentDigest;
	private Map<String, String> templateVariables;
	private List<ConfigurationTemplateMongo> configurationTemplates;
	private boolean outdated;
	private List<String> urls;
	private DesiredState desiredState;
}