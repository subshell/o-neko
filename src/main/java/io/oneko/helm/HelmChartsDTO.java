package io.oneko.helm;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HelmChartsDTO {

	@Data
	@Builder
	public static class HelmChartVersionDTO {
		private final String version;
		private final String appVersion;
		private final String description;
	}

	private UUID registryId;
	private Map<String, List<HelmChartVersionDTO>> charts;
}

