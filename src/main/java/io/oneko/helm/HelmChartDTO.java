package io.oneko.helm;

import lombok.Data;

@Data
public class HelmChartDTO {
	private String name;
	private String version;
	private String appVersion;
	private String description;
}

