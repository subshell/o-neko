package io.oneko.docker.v2.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.oneko.docker.DockerRegistry;
import io.oneko.metrics.MetricNameBuilder;
import lombok.Getter;

@Getter
public class MetersPerRegistry {
	private final Timer versionCheckTimerOk;
	private final Timer versionCheckTimerError;

	private final Timer listAllTagsTimerOk;
	private final Timer listAllTagsTimerError;

	private final Timer getManifestTimerOk;
	private final Timer getManifestTimerError;

	public MetersPerRegistry(DockerRegistry dockerRegistry, MeterRegistry meterRegistry) {
		versionCheckTimerOk = builder(dockerRegistry)
				.tag("operation", "versionCheck")
				.tag("result", "success")
				.register(meterRegistry);

		versionCheckTimerError = builder(dockerRegistry)
				.tag("operation", "versionCheck")
				.tag("result", "error")
				.register(meterRegistry);

		listAllTagsTimerOk = builder(dockerRegistry)
				.tag("operation", "listAllTags")
				.tag("result", "success")
				.register(meterRegistry);

		listAllTagsTimerError = builder(dockerRegistry)
				.tag("operation", "listAllTags")
				.tag("result", "error")
				.register(meterRegistry);

		getManifestTimerOk = builder(dockerRegistry)
				.tag("operation", "getManifest")
				.tag("result", "success")
				.register(meterRegistry);

		getManifestTimerError = builder(dockerRegistry)
				.tag("operation", "getManifest")
				.tag("result", "error")
				.register(meterRegistry);
	}

	private static Timer.Builder builder(DockerRegistry dockerRegistry) {
		return Timer.builder(new MetricNameBuilder().durationOf("docker.registry.client.request").build())
				.description("the time it takes to make requests to the container registry")
				.publishPercentileHistogram()
				.tag("registry", dockerRegistry.getName());
	}
}
