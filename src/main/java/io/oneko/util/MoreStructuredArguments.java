package io.oneko.util;

import io.oneko.docker.DockerRegistry;
import io.oneko.helm.HelmRegistry;
import io.oneko.project.Project;
import io.oneko.project.ProjectVersion;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.argument.StructuredArguments;

public class MoreStructuredArguments {

	public static final String PROJECT_KEY = "project";
	public static final String VERSION_KEY = "version";
	public static final String IMAGE_UPDATED_DATE_KEY = "version_image_updated_date";
	public static final String HELM_REGISTRY_KEY = "helm_registry";
	public static final String CONTAINER_REGISTRY_KEY = "container_registry";

	public static StructuredArgument projectKv(Project project) {
		return StructuredArguments.kv(PROJECT_KEY, project.getName());
	}

	public static StructuredArgument projectKv(String projectName) {
		return StructuredArguments.kv(PROJECT_KEY, projectName);
	}

	public static StructuredArgument projectV(Project project) {
		return StructuredArguments.v(PROJECT_KEY, project.getName());
	}

	public static StructuredArgument projectV(String projectName) {
		return StructuredArguments.v(PROJECT_KEY, projectName);
	}

	public static StructuredArgument versionKv(ProjectVersion version) {
		return StructuredArguments.kv(VERSION_KEY, version.getName());
	}

	public static StructuredArgument versionKv(String versionName) {
		return StructuredArguments.kv(VERSION_KEY, versionName);
	}

	public static StructuredArgument versionV(ProjectVersion version) {
		return StructuredArguments.v(VERSION_KEY, version.getName());
	}

	public static StructuredArgument versionV(String versionName) {
		return StructuredArguments.v(VERSION_KEY, versionName);
	}

	public static StructuredArgument helmRegistryKv(HelmRegistry helmRegistry) {
		return StructuredArguments.kv(HELM_REGISTRY_KEY, helmRegistry.getName());
	}

	public static StructuredArgument helmRegistryV(HelmRegistry helmRegistry) {
		return StructuredArguments.v(HELM_REGISTRY_KEY, helmRegistry.getName());
	}

	public static StructuredArgument containerRegistryKv(DockerRegistry dockerRegistry) {
		return StructuredArguments.kv(CONTAINER_REGISTRY_KEY, dockerRegistry.getName());
	}

	public static StructuredArgument containerRegistryV(DockerRegistry dockerRegistry) {
		return StructuredArguments.v(CONTAINER_REGISTRY_KEY, dockerRegistry.getName());
	}
}
