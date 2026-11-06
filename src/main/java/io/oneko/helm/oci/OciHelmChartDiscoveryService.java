package io.oneko.helm.oci;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.oneko.helm.HelmRegistry;
import io.oneko.helm.HelmRegistryException;
import io.oneko.helmapi.model.Chart;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OciHelmChartDiscoveryService {

	private final ObjectMapper objectMapper;

	public OciHelmChartDiscoveryService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * Best-effort discovery of all helm charts in an OCI registry via the registry's (optional) catalog endpoint.
	 * Returns an empty list, rather than failing, for registries that do not support it - callers should offer
	 * {@link #lookupChart(HelmRegistry, String)} as a fallback for those in a UI.
	 */
	public List<Chart> discoverCharts(HelmRegistry registry) {
		OciHelmRegistryClient client = new OciHelmRegistryClient(registry, objectMapper);
		List<Chart> charts = new ArrayList<>();
		for (String repository : client.listChartRepositories()) {
			try {
				charts.addAll(client.listChartsIn(repository));
			} catch (HelmRegistryException e) {
				log.warn("failed to list helm charts in OCI repository {} of registry {}", repository, registry.getName(), e);
			}
		}
		return charts;
	}

	/**
	 * Looks up the available versions of a single, already known chart, identified by its path relative to the
	 * registry's URL. Unlike {@link #discoverCharts(HelmRegistry)} this does not rely on the catalog endpoint and
	 * therefore also works against registries that don't support it (e.g. Docker Hub, GHCR, ECR).
	 */
	public List<Chart> lookupChart(HelmRegistry registry, String chartPath) throws HelmRegistryException {
		OciHelmRegistryClient client = new OciHelmRegistryClient(registry, objectMapper);
		return client.listChartsIn(client.resolveRepositoryPath(chartPath));
	}

}
