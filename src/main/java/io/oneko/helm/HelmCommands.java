package io.oneko.helm;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.oneko.helmapi.api.Helm;
import io.oneko.helmapi.model.Chart;
import io.oneko.helmapi.model.InstallStatus;
import io.oneko.helmapi.model.Release;
import io.oneko.helmapi.model.Status;
import io.oneko.helmapi.model.Values;
import io.oneko.helmapi.process.CommandException;
import io.oneko.metrics.MetricNameBuilder;
import io.oneko.project.ProjectVersion;
import io.oneko.templates.WritableConfigurationTemplate;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HelmCommands {

	private final Helm helm = new Helm();

	private final Counter commandErrorCounter;
	private final Timer addRepoTimer;
	private final Timer deleteRepoTimer;
	private final Timer repoUpdateTimer;
	private final Timer searchRepoTimer;
	private final Timer installTimer;
	private final Timer uninstallTimer;
	private final Timer statusTimer;

	private Instant lastRepoUpdate = Instant.MIN;


	public HelmCommands(MeterRegistry meterRegistry) {
		this.commandErrorCounter = Counter.builder(new MetricNameBuilder().amountOf("helm.command.errors").build())
				.description("number of errors during Helm command execution")
				.register(meterRegistry);
		this.addRepoTimer = timer("repo_add", meterRegistry);
		this.deleteRepoTimer = timer("repo_delete", meterRegistry);
		this.repoUpdateTimer = timer("repo_update", meterRegistry);
		this.searchRepoTimer = timer("search_repo", meterRegistry);
		this.installTimer = timer("install", meterRegistry);
		this.uninstallTimer = timer("uninstall", meterRegistry);
		this.statusTimer = timer("status", meterRegistry);
	}

	private Timer timer(String operation, MeterRegistry meterRegistry) {
		return Timer.builder(new MetricNameBuilder().durationOf("helm.command").build())
				.description("duration of Helm commands")
				.publishPercentileHistogram()
				.tag("operation", operation)
				.register(meterRegistry);
	}

	public void addRegistry(ReadableHelmRegistry helmRegistry) throws HelmRegistryException {
		try {
			addRepoTimer.record(() ->
					helm.addRepo(helmRegistry.getName(), helmRegistry.getUrl(), helmRegistry.getUsername(), helmRegistry.getPassword())
			);
		} catch (CommandException e) {
			commandErrorCounter.increment();
			throw HelmRegistryException.fromCommandException(e, helmRegistry.getUrl(), helmRegistry.getName());
		}
	}

	public void deleteRegistry(ReadableHelmRegistry helmRegistry) throws HelmRegistryException {
		try {
			deleteRepoTimer.record(() ->
					helm.removeRepo(helmRegistry.getName())
			);
		} catch (CommandException e) {
			commandErrorCounter.increment();
			throw HelmRegistryException.fromCommandException(e, helmRegistry.getUrl(), helmRegistry.getName());
		}
	}

	public synchronized void updateReposNotTooOften() {
		final Instant now = Instant.now();
		if (now.isBefore(lastRepoUpdate.plusSeconds(30))) {
			log.debug("not updating helm repos because the last update was less than 30 seconds ago");
			return;
		}
		lastRepoUpdate = now;
		repoUpdateTimer.record(helm::updateRepos);
	}

	public List<Chart> getCharts(ReadableHelmRegistry helmRegistry) throws HelmRegistryException {
		try {
			updateReposNotTooOften();
			return searchRepoTimer.record(() ->
					helm.searchRepo(helmRegistry.getName() + "/", true, false)
			);
		} catch (CommandException e) {
			commandErrorCounter.increment();
			throw HelmRegistryException.fromCommandException(e, helmRegistry.getUrl(), helmRegistry.getName());
		}
	}

	public List<InstallStatus> install(ProjectVersion<?, ?> projectVersion) throws HelmRegistryException {
		try {
			updateReposNotTooOften();
			final var sample = Timer.start();
			List<WritableConfigurationTemplate> calculatedConfigurationTemplates = projectVersion.getCalculatedConfigurationTemplates();
			List<InstallStatus> result = new ArrayList<>();
			for (int i = 0; i < calculatedConfigurationTemplates.size(); i++) {
				WritableConfigurationTemplate template = calculatedConfigurationTemplates.get(i);
				result.add(helm.install(getReleaseName(projectVersion, i), template.getChartName(), template.getChartVersion(), Values.fromYamlString(template.getContent()), projectVersion.getNamespaceOrElseFromProject(), false));
			}
			sample.stop(installTimer);
			return result;
		} catch (CommandException e) {
			commandErrorCounter.increment();
			throw new HelmRegistryException(e.getMessage());
		}
	}

	public void uninstall(List<String> releaseNames) throws HelmRegistryException {
		try {
			uninstallTimer.record(() ->
					helm.listInAllNamespaces().stream()
							.filter(release -> releaseNames.contains(release.getName()))
							.forEach(release -> helm.uninstall(release.getName(), release.getNamespace()))
			);
		} catch (CommandException e) {
			commandErrorCounter.increment();
			throw new HelmRegistryException(e.getMessage());
		}
	}

	public void uninstall(ProjectVersion<?, ?> projectVersion) throws HelmRegistryException {
		try {
			uninstallTimer.record(() ->
					helm.listInAllNamespaces().stream()
							.filter(release -> release.getName().startsWith(getReleaseNamePrefix(projectVersion)))
							.forEach(release -> helm.uninstall(release.getName(), release.getNamespace()))
			);
		} catch (CommandException e) {
			commandErrorCounter.increment();
			throw new HelmRegistryException(e.getMessage());
		}
	}

	public List<Status> status(ProjectVersion<?, ?> projectVersion) throws HelmRegistryException {
		try {
			return statusTimer.record(() ->
					helm.listInAllNamespaces()
							.stream()
							.filter(release -> release.getName().startsWith(getReleaseNamePrefix(projectVersion)))
							.map(release -> helm.status(release.getName(), release.getNamespace()))
							.collect(Collectors.toList())
			);
		} catch (CommandException e) {
			commandErrorCounter.increment();
			throw new HelmRegistryException(e.getMessage());
		}
	}

	private String getReleaseName(ProjectVersion<?, ?> projectVersion, int templateIndex) {
		final String fullReleaseName = getReleaseNamePrefix(projectVersion) + "-" + templateIndex + "-" + maxLength(Long.toString(System.currentTimeMillis()), 10);
		return sanitizeReleaseName(maxLength(fullReleaseName, 53));
	}

	public List<String> getReferencedHelmReleases(ProjectVersion<?, ?> projectVersion) {
		final String namespace = projectVersion.getNamespaceOrElseFromProject();
		final List<Release> list = helm.list(namespace, null);
		return list.stream()
				.map(Release::getName)
				.filter(name -> name.startsWith(getReleaseNamePrefix(projectVersion)))
				.collect(Collectors.toList());
	}

	@VisibleForTesting
	protected String getReleaseNamePrefix(ProjectVersion<?, ?> projectVersion) {
		var projectName = maxLength(projectVersion.getProject().getName(), 10);
		var projectId = maxLength(projectVersion.getProject().getId().toString(), 8);
		var versionName = maxLength(projectVersion.getName(), 10);
		var versionId = maxLength(projectVersion.getId().toString(), 8);
		return sanitizeReleaseName(String.format("%s%s-%s%s", projectName, projectId, versionName, versionId));
	}

	private String sanitizeReleaseName(String in) {
		String candidate = in.toLowerCase();
		candidate = candidate.replaceAll("_", "-");
		candidate = candidate.replaceAll("[^a-z0-9\\-]", StringUtils.EMPTY);//remove invalid chars (only alphanumeric and dash allowed)
		candidate = candidate.replaceAll("^[\\-]*", StringUtils.EMPTY);//remove invalid start (remove dot, dash and underscore from start)
		candidate = candidate.replaceAll("[\\-]*$", StringUtils.EMPTY);//remove invalid end (remove dot, dash and underscore from end)
		if (StringUtils.isBlank(candidate)) {
			throw new IllegalArgumentException("can not create a legal namespace name from " + in);
		}
		return candidate;
	}

	private String maxLength(String in, int length) {
		return in.substring(0, Math.min(in.length(), length));
	}
}
