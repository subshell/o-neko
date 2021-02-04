package io.oneko.helm.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.oneko.helm.HelmRegistryException;
import io.oneko.helm.ReadableHelmRegistry;
import io.oneko.helmapi.api.Helm;
import io.oneko.helmapi.model.Chart;
import io.oneko.helmapi.model.InstallStatus;
import io.oneko.helmapi.model.Release;
import io.oneko.helmapi.model.Status;
import io.oneko.helmapi.model.Values;
import io.oneko.helmapi.process.CommandException;
import io.oneko.project.ProjectVersion;
import io.oneko.templates.ConfigurationTemplate;
import io.oneko.templates.WritableConfigurationTemplate;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class HelmCommandUtils {

	private static final Helm helm = new Helm();
	private static Instant lastRepoUpdate = Instant.MIN;

	public static void addRegistry(ReadableHelmRegistry helmRegistry) throws HelmRegistryException {
		try {
			helm.addRepo(helmRegistry.getName(), helmRegistry.getUrl(), helmRegistry.getUsername(), helmRegistry.getPassword());
		} catch (CommandException e) {
			throw HelmRegistryException.fromCommandException(e, helmRegistry.getUrl(), helmRegistry.getName());
		}
	}

	public static void deleteRegistry(ReadableHelmRegistry helmRegistry) throws HelmRegistryException {
		try {
			helm.removeRepo(helmRegistry.getName());
		} catch (CommandException e) {
			throw HelmRegistryException.fromCommandException(e, helmRegistry.getUrl(), helmRegistry.getName());
		}
	}

	public static synchronized void updateReposNotTooOften() {
		final Instant now = Instant.now();
		if (now.isBefore(lastRepoUpdate.plusSeconds(30))) {
			log.debug("Not updating helm repos because the last update was less than 30 seconds ago");
			return;
		}
		lastRepoUpdate = now;
		helm.updateRepos();
	}

	public static List<Chart> getCharts(ReadableHelmRegistry helmRegistry) throws HelmRegistryException {
		try {
			updateReposNotTooOften();
			return helm.searchRepo(helmRegistry.getName() + "/", true, false);
		} catch (CommandException e) {
			throw HelmRegistryException.fromCommandException(e, helmRegistry.getUrl(), helmRegistry.getName());
		}
	}

	public static List<InstallStatus> install(ProjectVersion<?, ?> projectVersion) throws HelmRegistryException {
		try {
			updateReposNotTooOften();
			List<WritableConfigurationTemplate> calculatedConfigurationTemplates = projectVersion.getCalculatedConfigurationTemplates();
			List<InstallStatus> result = new ArrayList<>();
			for (int i = 0; i < calculatedConfigurationTemplates.size(); i++) {
				WritableConfigurationTemplate template = calculatedConfigurationTemplates.get(i);
				result.add(helm.install(getReleaseName(projectVersion, i), template.getChartName(), template.getChartVersion(), Values.fromYamlString(template.getContent()), projectVersion.getNamespaceOrElseFromProject(), false));
			}
			return result;
		} catch (CommandException e) {
			throw new HelmRegistryException(e.getMessage());
		}
	}

	public static void uninstall(List<String> releaseNames) throws HelmRegistryException {
		try {
			helm.listInAllNamespaces().stream()
					.filter(release -> releaseNames.contains(release.getName()))
					.forEach(release -> helm.uninstall(release.getName(), release.getNamespace()));
		} catch (CommandException e) {
			throw new HelmRegistryException(e.getMessage());
		}
	}

	public static void uninstall(ProjectVersion<?, ?> projectVersion) throws HelmRegistryException {
		try {
			helm.listInAllNamespaces().stream()
					.filter(release -> release.getName().startsWith(getReleaseNamePrefix(projectVersion)))
					.forEach(release -> helm.uninstall(release.getName(), release.getNamespace()));
		} catch (CommandException e) {
			throw new HelmRegistryException(e.getMessage());
		}
	}

	public static List<Status> status(ProjectVersion<?, ?> projectVersion) throws HelmRegistryException {
		try {
			return helm.listInAllNamespaces()
					.stream()
					.filter(release -> release.getName().startsWith(getReleaseNamePrefix(projectVersion)))
					.map(release -> helm.status(release.getName(), release.getNamespace()))
					.collect(Collectors.toList());
		} catch (CommandException e) {
			throw new HelmRegistryException(e.getMessage());
		}
	}

	public List<String> getKubernetesYamlResources(ProjectVersion<?, ?> projectVersion) {
		final String namespace = projectVersion.getNamespaceOrElseFromProject();
		final List<Release> list = helm.list(namespace, null);
		return list.stream().filter(release -> release.getName().startsWith(getReleaseNamePrefix(projectVersion)))
				.map(release -> {
					final Status status = helm.status(release.getName(), namespace);
					return status.getManifest();
				}).collect(Collectors.toList());
	}

	private static String getReleaseName(ProjectVersion<?, ?> projectVersion, int templateIndex) {
		final String fullReleaseName = getReleaseNamePrefix(projectVersion) + "-" + templateIndex + "-" + maxLength(Long.toString(System.currentTimeMillis()), 10);
		return sanitizeReleaseName(maxLength(fullReleaseName, 53));
	}

	private static String getReleaseNamePrefix(ProjectVersion<?, ?> projectVersion) {
		return sanitizeReleaseName(maxLength(projectVersion.getProject().getName(), 6) + "-" + maxLength(projectVersion.getName(), 32));
	}

	private static String sanitizeReleaseName(String in) {
		String candidate = in.toLowerCase();
		candidate = candidate.replaceAll("_", "-");
		candidate = candidate.replaceAll("[^a-z0-9\\-]", StringUtils.EMPTY);//remove invalid chars (only alphanumeric and dash allowed)
		candidate = candidate.replaceAll("^[\\-]*", StringUtils.EMPTY);//remove invalid start (remove dot, dash and underscore from start)
		candidate = candidate.replaceAll("[\\-]*$", StringUtils.EMPTY);//remove invalid end (remove dot, dash and underscore from end)
		if (StringUtils.isBlank(candidate)) {
			throw new IllegalArgumentException("Can not create a legal namespace from " + in);
		}
		return candidate;
	}

	private static String maxLength(String in, int length) {
		return in.substring(0, Math.min(in.length(), length));
	}
}
