package io.oneko.helm;

import static io.oneko.util.MoreStructuredArguments.*;
import static net.logstash.logback.argument.StructuredArguments.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import io.oneko.helmapi.model.Chart;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HelmCharts {
	private final LoadingCache<UUID, HelmChartsDTO> chartsCache;

	public HelmCharts(HelmRegistryRepository helmRegistryRepository, HelmCommands helmCommands, MeterRegistry meterRegistry) {
		this.chartsCache = Caffeine.newBuilder()
				.expireAfterWrite(3, TimeUnit.MINUTES)
				.recordStats()
				.build(registryId -> helmRegistryRepository.getById(registryId)
						.flatMap(helmRegistry -> {
							try {
								List<Chart> charts = helmCommands.getCharts(helmRegistry);
								log.debug("found helm charts ({}, {})", kv("chart_count", charts.size()), helmRegistryKv(helmRegistry));

								return toHelmChartDTO(helmRegistry, charts);
							} catch (HelmRegistryException e) {
								return Optional.empty();
							}
						})
						.orElseThrow(() -> new IllegalArgumentException("registry not found"))
				);
		CaffeineCacheMetrics.monitor(meterRegistry, chartsCache, "helmChartCache");
	}

	public Optional<HelmChartsDTO> getChartsByHelmRegistry(UUID registryId) {
		try {
			return Optional.of(chartsCache.get(registryId));
		} catch (RuntimeException e) {
			return Optional.empty();
		}
	}

	public void refreshHelmChartsInRegistry(UUID registryId) {
		chartsCache.refresh(registryId);
	}

	public void invalidateHelmChartsInRegistry(UUID registryId) {
		chartsCache.invalidate(registryId);
	}

	private Optional<HelmChartsDTO> toHelmChartDTO(HelmRegistry helmRegistry, List<Chart> charts) {
		if (charts.isEmpty()) {
			return Optional.empty();
		}

		Map<String, List<Chart>> chartsGroupByName = charts.stream().collect(Collectors.groupingBy(Chart::getName));

		Map<String, List<HelmChartsDTO.HelmChartVersionDTO>> chartVersionsByName = new HashMap<>();
		for (var chart : charts) {
			var chartVersions = chartsGroupByName.get(chart.getName()).stream().map(this::toHelmChartVersionDTO).collect(Collectors.toList());
			chartVersionsByName.put(chart.getName(), chartVersions);
		}

		return Optional.of(HelmChartsDTO.builder()
				.registryId(helmRegistry.getId())
				.charts(chartVersionsByName)
				.build());
	}

	private HelmChartsDTO.HelmChartVersionDTO toHelmChartVersionDTO(Chart chart) {
		return HelmChartsDTO.HelmChartVersionDTO.builder()
				.version(chart.getVersion())
				.appVersion(chart.getAppVersion())
				.description(chart.getDescription())
				.build();
	}

}
