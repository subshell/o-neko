package io.oneko.helm.oci;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * The config blob of a helm chart's OCI manifest is the chart's Chart.yaml serialized to JSON.
 * See <a href="https://helm.sh/docs/topics/registries/">the Helm OCI documentation</a>.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HelmOciChartConfig {
	private String name;
	private String version;
	private String appVersion;
	private String description;
}
