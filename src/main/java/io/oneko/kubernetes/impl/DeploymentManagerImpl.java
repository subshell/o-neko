package io.oneko.kubernetes.impl;

import static io.oneko.kubernetes.deployments.DesiredState.Deployed;
import static io.oneko.kubernetes.deployments.DesiredState.NotDeployed;
import static io.oneko.util.MoreStructuredArguments.versionKv;
import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import io.oneko.docker.event.ObsoleteProjectVersionRemovedEvent;
import io.oneko.docker.v2.DockerRegistryClientFactory;
import io.oneko.docker.v2.model.manifest.Manifest;
import io.oneko.event.Event;
import io.oneko.event.EventDispatcher;
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

	/**
	 * version id -> number of errors
	 */
	private final Map<UUID, Integer> deploymentErrors = new ConcurrentHashMap<>();

	DeploymentManagerImpl(DockerRegistryClientFactory dockerRegistryClientFactory,
												ProjectRepository projectRepository,
												DeploymentRepository deploymentRepository,
												EventDispatcher eventDispatcher) {
		this.dockerRegistryClientFactory = dockerRegistryClientFactory;
		this.projectRepository = projectRepository;
		this.deploymentRepository = deploymentRepository;
		eventDispatcher.registerListener(this::consumeDeletedVersionEvent);
	}

	@Override
	public ReadableProjectVersion deploy(WritableProjectVersion version) {
		if (StringUtils.isBlank(version.getNamespaceOrElseFromProject())) {
			throw new RuntimeException("A namespace must be configured in the project.");
		}

		final UUID versionId = version.getId();
		if (deploymentErrors.containsKey(versionId)) {
			log.warn("Deployment of version {} already failed {} times.",
					versionKv(version), kv("error_count", deploymentErrors.get(versionId)));
		}

		try {
			final WritableDeployment deployment = getOrCreateDeploymentForVersion(version);

			if (!deployment.getReleaseNames().isEmpty()) {
				HelmCommandUtils.uninstall(deployment.getReleaseNames());
			}

			final List<InstallStatus> installStatuses = HelmCommandUtils.install(version);
			final List<String> releaseNames = installStatuses.stream().map(Status::getName).collect(Collectors.toList());
			deployment.setReleaseNames(releaseNames);
			deploymentRepository.save(deployment);

			version = updateDeployableWithCreatedResources(version);
			version.setDesiredState(Deployed);

			final ReadableProject project = projectRepository.add(version.getProject());
			deploymentErrors.remove(versionId);
			return project.getVersions().stream()
					.filter(projectVersion -> projectVersion.getUuid().equals(versionId))
					.findFirst()
					.orElse(null);
		} catch (HelmRegistryException e) {
			log.error("failed to deploy ({})", versionKv(version), e);
			countDeploymentError(version.getId());
			throw new RuntimeException(e);
		}
	}

	@Override
	public void rollback(WritableProjectVersion version) {
		final WritableDeployment deployment = getOrCreateDeploymentForVersion(version);
		if (!deployment.getReleaseNames().isEmpty()) {
			try {
				HelmCommandUtils.uninstall(deployment.getReleaseNames());
			} catch (HelmRegistryException ex) {
				log.error("rollback deployment of {} failed", versionKv(version) , ex);
				countDeploymentError(version.getId());
				throw new RuntimeException(ex);
			}
		}
	}

	private int countDeploymentError(UUID versionId) {
		return deploymentErrors.compute(versionId, (key, value) -> value == null ? 1 : value + 1);
	}

	private WritableDeployment getOrCreateDeploymentForVersion(ProjectVersion<?, ?> projectVersion) {
		return deploymentRepository.findByProjectVersionId(projectVersion.getId())
				.map(ReadableDeployment::writable)
				.orElseGet(() -> WritableDeployment.getDefaultDeployment(projectVersion.getId()));
	}

	private WritableProjectVersion updateDeployableWithCreatedResources(WritableProjectVersion deployable) {
		deployable.setOutdated(false);
		if (!StringUtils.isEmpty(deployable.getDockerContentDigest())) {
			return deployable;
		}

		return dockerRegistryClientFactory.getDockerRegistryClient(deployable.getProject())
				.map(client -> {
					final Manifest manifest = client.getManifest(deployable);
					deployable.setDockerContentDigest(manifest.getDockerContentDigest());
					return deployable;
				}).orElse(null);
	}

	@Override
	public ReadableProjectVersion stopDeployment(WritableProjectVersion version) {
		try {
			final WritableDeployment deployment = getOrCreateDeploymentForVersion(version);
			HelmCommandUtils.uninstall(version);
			deploymentRepository.deleteById(deployment.getId());
			version.setDesiredState(NotDeployed);
			final ReadableProject readableProject = projectRepository.add(version.getProject());
			return readableProject.getVersions().stream()
					.filter(projectVersion -> projectVersion.getUuid().equals(version.getId()))
					.findFirst().orElse(null);
		} catch (HelmRegistryException e) {
			log.error("failed to stop deployment ({})", versionKv(version), e);
			countDeploymentError(version.getId());
			throw new RuntimeException(e);
		}
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
