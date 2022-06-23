package io.oneko.docker.v2.metrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.oneko.docker.DockerRegistry;
import lombok.Getter;

@Component
@Getter
public class DockerRegistryClientMetrics {
	private final MeterRegistry meterRegistry;
	private static Map<UUID, MetersPerRegistry> metersByRegistry = Collections.synchronizedMap(new HashMap<>());

	DockerRegistryClientMetrics(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	public MetersPerRegistry getMeters(DockerRegistry dockerRegistry) {
		return metersByRegistry.computeIfAbsent(dockerRegistry.getUuid(), ignored -> new MetersPerRegistry(dockerRegistry, meterRegistry));
	}

}
