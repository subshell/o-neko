package io.oneko.helm.model;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InstallStatus extends Status {

	private Map<String, Object> config;
	private ChartInfo chart;

	@Data
	@NoArgsConstructor
	public static class ChartInfo {
		private ChartMetadata metadata;
		private Object lock;
		private List<Template> templates;
		private Map<String, Object> values;
		private Object schema;
		private List<Template> files;
	}
	@Data
	@NoArgsConstructor
	public static class ChartMetadata {
		private String name;
		private List<String> sources;
		private String version;
		private String description;
		private List<Maintainer> maintainers;
		private String apiVersion;
		private String appVersion;
		private String type;
	}
	@Data
	@NoArgsConstructor
	public static class Maintainer {
		private String name;
		private String email;
	}
	@Data
	@NoArgsConstructor
	public static class Template {
		private String name;
		private String data;
	}
}
