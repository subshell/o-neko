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
import io.oneko.helm.oci.OciHelmChartDiscoveryService;
import io.oneko.helmapi.model.Chart;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HelmCharts {
	private final HelmRegistryRepository helmRegistryRepository;
	private final OciHelmChartDiscoveryService ociHelmChartDiscoveryService;
	private final LoadingCache<UUID, HelmChartsDTO> chartsCache;

	public HelmCharts(HelmRegistryRepository helmRegistryRepository, HelmCommands helmCommands,
										 OciHelmChartDiscoveryService ociHelmChartDiscoveryService, MeterRegistry meterRegistry) {
		this.helmRegistryRepository = helmRegistryRepository;
		this.ociHelmChartDiscoveryService = ociHelmChartDiscoveryService;
		this.chartsCache = Caffeine.newBuilder()
				.expireAfterWrite(3, TimeUnit.MINUTES)
				.recordStats()
				.build(registryId -> helmRegistryRepository.getById(registryId)
						.flatMap(helmRegistry -> {
							try {
								if (!helmRegistry.isOCIRegistry()) {
									List<Chart> charts = helmCommands.getCharts(helmRegistry);
									log.debug("found helm charts ({}, {})", kv("chart_count", charts.size()), helmRegistryKv(helmRegistry));

									return toHelmChartDTO(helmRegistry, charts);
								} else {
									List<Chart> charts = ociHelmChartDiscoveryService.discoverCharts(helmRegistry);
									log.debug("found oci helm charts ({}, {})", kv("chart_count", charts.size()), helmRegistryKv(helmRegistry));

									return toHelmChartDTO(helmRegistry, charts);
								}
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

	/**
	 * Looks up the available versions of a single, known chart in an OCI registry by its path relative to the
	 * registry's URL. This is the fallback for OCI registries whose catalog endpoint isn't supported (and therefore
	 * doesn't show up via {@link #getChartsByHelmRegistry(UUID)}), e.g. Docker Hub, GHCR or ECR.
	 */
	public List<HelmChartsDTO.HelmChartVersionDTO> lookupOciChartVersions(UUID registryId, String chartPath) throws HelmRegistryException {
		ReadableHelmRegistry registry = helmRegistryRepository.getById(registryId)
				.orElseThrow(() -> new HelmRegistryException("Helm registry " + registryId + " not found"));
		if (!registry.isOCIRegistry()) {
			throw new HelmRegistryException("Helm registry " + registry.getName() + " is not an OCI registry");
		}
		return ociHelmChartDiscoveryService.lookupChart(registry, chartPath).stream()
				.map(this::toHelmChartVersionDTO)
				.collect(Collectors.toList());
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
