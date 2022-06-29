package io.oneko.kubernetes.impl;

import static io.oneko.kubernetes.deployments.DesiredState.Deployed;
import static io.oneko.kubernetes.deployments.DesiredState.NotDeployed;
import static io.oneko.util.MoreStructuredArguments.versionKv;
import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import io.oneko.docker.event.ObsoleteProjectVersionRemovedEvent;
import io.oneko.docker.v2.DockerRegistryClientFactory;
import io.oneko.docker.v2.model.manifest.Manifest;
import io.oneko.event.DeploymentRollbackEvent;
import io.oneko.event.Event;
import io.oneko.event.EventDispatcher;
import io.oneko.event.HelmReleasesInstallEvent;
import io.oneko.helm.HelmRegistryException;
import io.oneko.helm.util.HelmCommandUtils;
import io.oneko.helmapi.model.InstallStatus;
import io.oneko.helmapi.model.Status;
import io.oneko.kubernetes.DeploymentManager;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import io.oneko.kubernetes.deployments.ReadableDeployment;
import io.oneko.kubernetes.deployments.WritableDeployment;
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


	DeploymentManagerImpl(DockerRegistryClientFactory dockerRegistryClientFactory,
												ProjectRepository projectRepository,
												DeploymentRepository deploymentRepository,
												EventDispatcher eventDispatcher, ProjectVersionLock projectVersionLock) {
		this.dockerRegistryClientFactory = dockerRegistryClientFactory;
		this.projectRepository = projectRepository;
		this.deploymentRepository = deploymentRepository;
		this.projectVersionLock = projectVersionLock;
		this.eventDispatcher = eventDispatcher;
		eventDispatcher.registerListener(this::consumeDeletedVersionEvent);
	}

	@Override
	public ReadableProjectVersion deploy(final WritableProjectVersion version) {
		if (StringUtils.isBlank(version.getNamespaceOrElseFromProject())) {
			throw new RuntimeException("A namespace must be configured in the project.");
		}

		final UUID versionId = version.getId();

		return projectVersionLock.doWithProjectVersionLock(version, () -> {
			try {
				final WritableDeployment deployment = getOrCreateDeploymentForVersion(version);

				if (!deployment.getReleaseNames().isEmpty()) {
					HelmCommandUtils.uninstall(deployment.getReleaseNames());
				}

				final List<InstallStatus> installStatuses = HelmCommandUtils.install(version);
				log.info("installing helm releases ({}, {})",
						kv("helm_releases", deployment.getReleaseNames()), versionKv(version));

				final List<String> releaseNames = installStatuses.stream().map(Status::getName).collect(Collectors.toList());
				deployment.setReleaseNames(releaseNames);
				deploymentRepository.save(deployment);

				eventDispatcher.dispatch(new HelmReleasesInstallEvent(version, releaseNames));

				return updateDeployableWithCreatedResources(version).map(newVersion -> {
					newVersion.setDesiredState(Deployed);

					final ReadableProject project = projectRepository.add(newVersion.getProject());
					return project.getVersions().stream()
							.filter(projectVersion -> projectVersion.getUuid().equals(versionId))
							.findFirst()
							.orElse(null);
				}).orElseThrow(() -> new RuntimeException("failed to update deployment from new version"));
			} catch (Exception e) {
				log.error("failed to deploy ({})", versionKv(version), e);
				rollback(version, e);
				throw new RuntimeException(e);
			}
		});
	}

	private void rollback(WritableProjectVersion version, Exception e) {
		// In case a deployment has not been deleted properly
		try {
			eventDispatcher.dispatch(new DeploymentRollbackEvent(version, e.getMessage()));
			final WritableDeployment deployment = getOrCreateDeploymentForVersion(version);
			final List<String> referencedHelmReleases = HelmCommandUtils.getReferencedHelmReleases(version);
			log.info("Found these helm releases for rollback: {}", kv("helm_releases", referencedHelmReleases));

			if (!referencedHelmReleases.isEmpty()) {
				log.info("starting rollback for project version {}", versionKv(version));

				if (!CollectionUtils.isEqualCollection(deployment.getReleaseNames(), referencedHelmReleases)) {
					log.warn("Orphaned helm release for project version {} detected. It will be removed.", versionKv(version));
				}
				HelmCommandUtils.uninstall(referencedHelmReleases);
				deployment.setReleaseNames(new ArrayList<>());
				deploymentRepository.save(deployment);
			}
		} catch (Exception e2) {
			log.error("rollback deployment of {} failed", versionKv(version) , e2);
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
		return projectVersionLock.doWithProjectVersionLock(version, () -> {
			try {
				final WritableDeployment deployment = getOrCreateDeploymentForVersion(version);
				HelmCommandUtils.uninstall(version);
				deploymentRepository.deleteById(deployment.getId());
				version.setDesiredState(NotDeployed);
				final ReadableProject readableProject = projectRepository.add(version.getProject());

				log.info("stopping helm releases {} for {}",
						kv("helm_releases", deployment.getReleaseNames()), versionKv(version));

				return readableProject.getVersions().stream()
						.filter(projectVersion -> projectVersion.getUuid().equals(version.getId()))
						.findFirst().orElse(null);
			} catch (HelmRegistryException e) {
				log.error("failed to stop deployment ({})", versionKv(version), e);
				throw new RuntimeException(e);
			}
		});
	}

	private void stopDeploymentOfRemovedVersion(ProjectVersion<?, ?> version) {
		try {
			final WritableDeployment deployment = getOrCreateDeploymentForVersion(version);
			HelmCommandUtils.uninstall(version);
			deploymentRepository.deleteById(deployment.getId());
		} catch (HelmRegistryException e) {
			log.error("failed to stop deployment of removed version ({})", versionKv(version), e);
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
