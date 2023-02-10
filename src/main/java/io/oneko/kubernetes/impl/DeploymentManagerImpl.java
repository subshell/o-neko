package io.oneko.kubernetes.impl;

import static io.oneko.kubernetes.deployments.DesiredState.*;
import static io.oneko.util.MoreStructuredArguments.*;
import static net.logstash.logback.argument.StructuredArguments.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.oneko.docker.event.ObsoleteProjectVersionRemovedEvent;
import io.oneko.docker.v2.DockerRegistryClientFactory;
import io.oneko.docker.v2.model.manifest.Manifest;
import io.oneko.event.DeploymentRollbackEvent;
import io.oneko.event.Event;
import io.oneko.event.EventDispatcher;
import io.oneko.event.HelmReleasesInstallEvent;
import io.oneko.helm.HelmCommands;
import io.oneko.helm.HelmRegistryException;
import io.oneko.helmapi.model.InstallStatus;
import io.oneko.helmapi.model.Status;
import io.oneko.kubernetes.DeploymentManager;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import io.oneko.kubernetes.deployments.ReadableDeployment;
import io.oneko.kubernetes.deployments.WritableDeployment;
import io.oneko.metrics.MetricNameBuilder;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.project.ProjectVersionLock;
import io.oneko.project.ReadableProject;
import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.WritableProjectVersion;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
class DeploymentManagerImpl implements DeploymentManager {

	private final DockerRegistryClientFactory dockerRegistryClientFactory;
	private final ProjectRepository projectRepository;
	private final DeploymentRepository deploymentRepository;
	private final ProjectVersionLock projectVersionLock;
	private final EventDispatcher eventDispatcher;
	private final HelmCommands helmCommands;

	private final Timer deployDurationTimer;
	private final Timer stopDeploymentDurationTimer;
	private final Counter startDeploymentErrors;
	private final Counter startDeploymentRejections;
	private final Counter stopDeploymentErrors;
	private final Counter stopDeploymentRejections;
	private final ExecutorService executor = Executors.newCachedThreadPool();

	DeploymentManagerImpl(DockerRegistryClientFactory dockerRegistryClientFactory,
												ProjectRepository projectRepository,
												DeploymentRepository deploymentRepository,
												EventDispatcher eventDispatcher,
												ProjectVersionLock projectVersionLock,
												HelmCommands helmCommands,
												MeterRegistry meterRegistry) {
		this.dockerRegistryClientFactory = dockerRegistryClientFactory;
		this.projectRepository = projectRepository;
		this.deploymentRepository = deploymentRepository;
		this.projectVersionLock = projectVersionLock;
		this.eventDispatcher = eventDispatcher;
		this.helmCommands = helmCommands;
		eventDispatcher.registerListener(this::consumeDeletedVersionEvent);

		deployDurationTimer = Timer.builder(new MetricNameBuilder().durationOf("kubernetes.deployment.action").build())
				.tag("action", "start")
				.publishPercentileHistogram()
				.register(meterRegistry);

		stopDeploymentDurationTimer = Timer.builder(new MetricNameBuilder().durationOf("kubernetes.deployment.action").build())
				.tag("action", "stop")
				.publishPercentileHistogram()
				.register(meterRegistry);

		startDeploymentErrors = Counter.builder(new MetricNameBuilder().amountOf("kubernetes.deployment.errors").build())
				.tag("action", "start")
				.register(meterRegistry);

		stopDeploymentErrors = Counter.builder(new MetricNameBuilder().amountOf("kubernetes.deployment.errors").build())
				.tag("action", "stop")
				.register(meterRegistry);

		startDeploymentRejections = Counter.builder(new MetricNameBuilder().amountOf("kubernetes.deployment.rejections").build())
				.tag("action", "start")
				.register(meterRegistry);

		stopDeploymentRejections = Counter.builder(new MetricNameBuilder().amountOf("kubernetes.deployment.rejections").build())
				.tag("action", "stop")
				.register(meterRegistry);
	}

	@Override
	public ReadableProjectVersion deploy(final WritableProjectVersion version) {
		final Timer.Sample sample = Timer.start();
		if (StringUtils.isBlank(version.getNamespaceOrElseFromProject())) {
			throw new RuntimeException("A namespace must be configured in the project.");
		}

		final UUID versionId = version.getId();

		return projectVersionLock.doWithProjectVersionLock(version, () -> {
			try {
				final WritableDeployment deployment = getOrCreateDeploymentForVersion(version);

				if (!deployment.getReleaseNames().isEmpty()) {
					helmCommands.uninstall(deployment.getReleaseNames(), true);
				}

				version.setDesiredState(Deployed);
				projectRepository.add(version.getProject());

				log.info("installing helm releases ({}, {})",
						kv("helm_releases", deployment.getReleaseNames()), versionKv(version));
				final List<InstallStatus> installStatuses = helmCommands.install(version, true);

				final List<String> releaseNames = installStatuses.stream().map(Status::getName).collect(Collectors.toList());
				deployment.setReleaseNames(releaseNames);
				deploymentRepository.save(deployment);

				eventDispatcher.dispatch(new HelmReleasesInstallEvent(version, releaseNames));

				final ReadableProjectVersion readableProjectVersion = updateDeployableWithCreatedResources(version).map(newVersion -> {
					final ReadableProject project = projectRepository.add(newVersion.getProject());
					return project.getVersions().stream()
							.filter(projectVersion -> projectVersion.getUuid().equals(versionId))
							.findFirst()
							.orElse(null);
				}).orElseThrow(() -> new RuntimeException("failed to update deployment from new version"));
				sample.stop(deployDurationTimer);
				return readableProjectVersion;
			} catch (Exception e) {
				log.error("failed to deploy ({})", versionKv(version), e);
				startDeploymentErrors.increment();
				rollback(version, e);
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public CompletableFuture<ReadableProjectVersion> deployAsync(WritableProjectVersion version) {
		if (projectVersionLock.isVersionLocked(version.getUuid())) {
			startDeploymentRejections.increment();
			log.warn("rejected action on project version because it is already being started or stopped at this moment ({}, {})", versionKv(version), kv("action", "start"));
			return CompletableFuture.failedFuture(new ConcurrentDeploymentException("This version is already being deployed or stopped at this moment. Try again later."));
		}

		return CompletableFuture.supplyAsync(() -> deploy(version), executor);
	}

	private void rollback(WritableProjectVersion version, Exception e) {
		// In case a deployment has not been deleted properly
		try {
			eventDispatcher.dispatch(new DeploymentRollbackEvent(version, e.getMessage()));
			final WritableDeployment deployment = getOrCreateDeploymentForVersion(version);
			final List<String> referencedHelmReleases = helmCommands.getReferencedHelmReleases(version);
			log.info("Found these helm releases for rollback: {}", kv("helm_releases", referencedHelmReleases));

			if (!referencedHelmReleases.isEmpty()) {
				log.info("starting rollback for project version {}", versionKv(version));

				if (!CollectionUtils.isEqualCollection(deployment.getReleaseNames(), referencedHelmReleases)) {
					log.warn("Orphaned helm release for project version {} detected. It will be removed.", versionKv(version));
				}
				helmCommands.uninstall(referencedHelmReleases, false);
				deployment.setReleaseNames(new ArrayList<>());
				deploymentRepository.save(deployment);
			}
		} catch (Exception e2) {
			log.error("rollback deployment of {} failed", versionKv(version), e2);
			stopDeploymentErrors.increment();
			throw new RuntimeException(e);
		}
	}

	private WritableDeployment getOrCreateDeploymentForVersion(ProjectVersion<?, ?> projectVersion) {
		return deploymentRepository.findByProjectVersionId(projectVersion.getId())
				.map(ReadableDeployment::writable)
				.orElseGet(() -> WritableDeployment.getDefaultDeployment(projectVersion.getId()));
	}

	private Optional<WritableProjectVersion> updateDeployableWithCreatedResources(WritableProjectVersion deployable) {
		deployable.setOutdated(false);
		if (!StringUtils.isEmpty(deployable.getDockerContentDigest())) {
			return Optional.of(deployable);
		}

		return dockerRegistryClientFactory.getDockerRegistryClient(deployable.getProject())
				.map(client -> {
					final Manifest manifest = client.getManifest(deployable);
					deployable.setDockerContentDigest(manifest.getDockerContentDigest());
					return deployable;
				});
	}

	@Override
	public ReadableProjectVersion stopDeployment(final WritableProjectVersion version) {
		final Timer.Sample sample = Timer.start();
		return projectVersionLock.doWithProjectVersionLock(version, () -> {
			try {
				final WritableDeployment deployment = getOrCreateDeploymentForVersion(version);
				helmCommands.uninstall(version, true);
				deploymentRepository.deleteById(deployment.getId());
				version.setDesiredState(NotDeployed);
				final ReadableProject readableProject = projectRepository.add(version.getProject());

				log.info("stopping helm releases ({}, {})",
						kv("helm_releases", deployment.getReleaseNames()), versionKv(version));

				final ReadableProjectVersion readableProjectVersion = readableProject.getVersions().stream()
						.filter(projectVersion -> projectVersion.getUuid().equals(version.getId()))
						.findFirst().orElse(null);
				sample.stop(stopDeploymentDurationTimer);
				return readableProjectVersion;
			} catch (HelmRegistryException e) {
				log.error("failed to stop deployment ({})", versionKv(version), e);
				stopDeploymentErrors.increment();
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public CompletableFuture<ReadableProjectVersion> stopDeploymentAsync(WritableProjectVersion version) {
		if (projectVersionLock.isVersionLocked(version.getUuid())) {
			stopDeploymentRejections.increment();
			log.warn("rejected action on project version because it is already being started or stopped at this moment ({}, {})", versionKv(version), kv("action", "stop"));
			return CompletableFuture.failedFuture(new ConcurrentDeploymentException("This version is already being deployed or stopped at this moment. Try again later."));
		}

		return CompletableFuture.supplyAsync(() -> stopDeployment(version), executor);
	}

	private void stopDeploymentOfRemovedVersion(ProjectVersion<?, ?> version) {
		final Timer.Sample sample = Timer.start();
		try {
			final WritableDeployment deployment = getOrCreateDeploymentForVersion(version);
			helmCommands.uninstall(version, false);
			deploymentRepository.deleteById(deployment.getId());
			sample.stop(stopDeploymentDurationTimer);
		} catch (HelmRegistryException e) {
			log.error("failed to stop deployment of removed version ({})", versionKv(version), e);
			stopDeploymentErrors.increment();
			throw new RuntimeException(e);
		}
	}

	private void consumeDeletedVersionEvent(Event event) {
		if (event instanceof ObsoleteProjectVersionRemovedEvent) {
			var e = (ObsoleteProjectVersionRemovedEvent) event;
			if (deploymentRepository.findByProjectVersionId(e.getVersionId()).isPresent()) {
				log.info("stopping obsolete deployment {}", versionKv(e.getVersion()));
				stopDeploymentOfRemovedVersion(e.getVersion());
			}
		}
	}
}
