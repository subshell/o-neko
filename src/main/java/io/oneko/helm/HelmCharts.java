package io.oneko.helm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import io.oneko.helm.util.HelmRegistryCommandUtils;
import io.oneko.helmapi.model.Chart;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HelmCharts {

	private final HelmRegistryRepository helmRegistryRepository;

	private static final long DURATION_BETWEEN_SEARCH_MS = 12 * 60 * 60 * 1000L;

	private final LoadingCache<UUID, HelmChartsDTO> chartsCache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.build(new CacheLoader<>() {
				@Override
				public HelmChartsDTO load(UUID registryId) {
					return helmRegistryRepository.getById(registryId)
							.flatMap(helmRegistry -> {
								try {
									List<Chart> charts = HelmRegistryCommandUtils.getCharts(helmRegistry);
									log.debug("Found {} helm charts in helm registry {}", charts.size(), helmRegistry.getName());

									return toHelmChartDTO(helmRegistry, charts);
								} catch (HelmRegistryException e) {
									return Optional.empty();
								}
							})
							.orElseThrow(() -> new IllegalArgumentException("Registry not found"));
				}
			});

	public HelmCharts(HelmRegistryRepository helmRegistryRepository) {
		this.helmRegistryRepository = helmRegistryRepository;
	}

	@Scheduled(fixedRate = DURATION_BETWEEN_SEARCH_MS, initialDelay = 0)
	public void fetchHelmCharts() {
		chartsCache.invalidateAll();
		helmRegistryRepository.getAll().forEach(helmRegistry -> this.refreshHelmChartsInRegistry(helmRegistry.getId()));
	}

	public Optional<HelmChartsDTO> getChartsByHelmRegistry(UUID registryId) {
		try {
			return Optional.of(chartsCache.get(registryId));
		} catch (ExecutionException e) {
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
