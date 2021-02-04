package io.oneko.kubernetes.impl;

import static io.oneko.kubernetes.deployments.DesiredState.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.oneko.docker.v2.DockerRegistryClientFactory;
import io.oneko.docker.v2.model.manifest.Manifest;
import io.oneko.helm.HelmRegistryException;
import io.oneko.helm.util.HelmCommandUtils;
import io.oneko.helmapi.model.InstallStatus;
import io.oneko.helmapi.model.Status;
import io.oneko.kubernetes.DeploymentManager;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import io.oneko.kubernetes.deployments.DesiredState;
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

	private final KubernetesAccess kubernetesAccess;
	private final DockerRegistryClientFactory dockerRegistryClientFactory;
	private final ProjectRepository projectRepository;
	private final DeploymentRepository deploymentRepository;

	DeploymentManagerImpl(KubernetesAccess kubernetesAccess,
												DockerRegistryClientFactory dockerRegistryClientFactory,
												ProjectRepository projectRepository,
												DeploymentRepository deploymentRepository) {
		this.kubernetesAccess = kubernetesAccess;
		this.dockerRegistryClientFactory = dockerRegistryClientFactory;
		this.projectRepository = projectRepository;
		this.deploymentRepository = deploymentRepository;
	}

	@Override
	public ReadableProjectVersion deploy(WritableProjectVersion version) {
		if (StringUtils.isBlank(version.getNamespaceOrElseFromProject())) {
			throw new RuntimeException("A namespace must be configured in the project.");
		}
		try {
			final UUID versionId = version.getId();
			final WritableDeployment deployment = getOrCreateDeploymentForVersion(version);

			if (!deployment.getReleaseNames().isEmpty()) {
				HelmCommandUtils.uninstall(deployment.getReleaseNames());
			}

			final List<InstallStatus> installStatuses = HelmCommandUtils.install(version);
			final List<String> releaseNames = installStatuses.stream().map(Status::getName).collect(Collectors.toList());
			deployment.setReleaseNames(releaseNames);
			deploymentRepository.save(deployment);

			final Set<HasMetadata> createdKubernetesResources = getTemplateAsResources(version);
			version = updateDeployableWithCreatedResources(version, createdKubernetesResources);
			version.setDesiredState(Deployed);
			final ReadableProject project = projectRepository.add(version.getProject());
			return project.getVersions().stream()
					.filter(projectVersion -> projectVersion.getUuid().equals(versionId))
					.findFirst()
					.orElse(null);
		} catch (HelmRegistryException e) {
			log.error("Failed to deploy version {}", version, e);
			throw new RuntimeException(e);
		}
	}

	private WritableDeployment getOrCreateDeploymentForVersion(ProjectVersion<?, ?> projectVersion) {
		return deploymentRepository.findByProjectVersionId(projectVersion.getId())
				.map(ReadableDeployment::writable)
				.orElseGet(() -> WritableDeployment.getDefaultDeployment(projectVersion.getId()));
	}

	private WritableProjectVersion updateDeployableWithCreatedResources(WritableProjectVersion deployable, Set<HasMetadata> createdResources) {
		updateDeploymentUrls(deployable, createdResources);
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

	private Set<HasMetadata> getTemplateAsResources(ProjectVersion<?, ?> deployable) {
		Set<HasMetadata> resources = new HashSet<>();
		HelmCommandUtils.getKubernetesYamlResources(deployable).forEach(template -> resources.addAll(kubernetesAccess.loadResource(template)));
		return resources;
	}

	private void updateDeploymentUrls(WritableProjectVersion deployable, Set<HasMetadata> createdResources) {
		List<String> urls = createdResources.stream()
				.flatMap(hasMetadata -> {
					if (hasMetadata instanceof Ingress) {
						var ingress = (Ingress) hasMetadata;
						return ingress
								.getSpec()
								.getRules()
								.stream()
								.map(IngressRule::getHost);
					} else if (hasMetadata instanceof io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress) {
						var ingress = (io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress) hasMetadata;
						return ingress
								.getSpec()
								.getRules()
								.stream()
								.map(io.fabric8.kubernetes.api.model.networking.v1beta1.IngressRule::getHost);
					} else if (hasMetadata instanceof io.fabric8.kubernetes.api.model.networking.v1.Ingress) {
						var ingress = (io.fabric8.kubernetes.api.model.networking.v1.Ingress) hasMetadata;
						return ingress
								.getSpec()
								.getRules()
								.stream()
								.map(io.fabric8.kubernetes.api.model.networking.v1.IngressRule::getHost);
					}
					return Stream.empty();
				}).collect(Collectors.toList());

		log.trace("Found urls {} of {} {}", urls, deployable.getClass().getSimpleName(), deployable.getName());
		deployable.setUrls(urls);
	}

	@Override
	public ReadableProjectVersion stopDeployment(WritableProjectVersion version) {
		try {
			HelmCommandUtils.uninstall(version);
			version.setUrls(List.of());
			version.setDesiredState(NotDeployed);
			final ReadableProject readableProject = projectRepository.add(version.getProject());
			return readableProject.getVersions().stream()
					.filter(projectVersion -> projectVersion.getUuid().equals(version.getId()))
					.findFirst().orElse(null);
		} catch (HelmRegistryException e) {
			log.error("Failed to stop deployment of version {}", version, e);
			throw new RuntimeException(e);
		}
	}
}
