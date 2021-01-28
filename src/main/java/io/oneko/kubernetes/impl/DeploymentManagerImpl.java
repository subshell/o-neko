package io.oneko.kubernetes.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.oneko.docker.DockerRegistry;
import io.oneko.docker.DockerRegistryRepository;
import io.oneko.docker.v2.DockerRegistryClientFactory;
import io.oneko.kubernetes.DeploymentManager;
import io.oneko.kubernetes.deployments.Deployable;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.WritableProjectVersion;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
class DeploymentManagerImpl implements DeploymentManager {

	private final KubernetesAccess kubernetesAccess;
	private final DockerRegistryClientFactory dockerRegistryClientFactory;
	private final DockerRegistryRepository dockerRegistryRepository;
	private final ProjectRepository projectRepository;

	DeploymentManagerImpl(KubernetesAccess kubernetesAccess, DockerRegistryClientFactory dockerRegistryClientFactory,
												DockerRegistryRepository dockerRegistryRepository, ProjectRepository projectRepository) {
		this.kubernetesAccess = kubernetesAccess;
		this.dockerRegistryClientFactory = dockerRegistryClientFactory;
		this.dockerRegistryRepository = dockerRegistryRepository;
		this.projectRepository = projectRepository;
	}

	@Override
	public ReadableProjectVersion deploy(WritableProjectVersion version) {
		/*
		try {
			log.debug("Deploying version {} of project {}", version.getName(), version.getProject().getName());
			Deployable<WritableProjectVersion> deployableVersion = Deployables.of(version);
			String namespace = version.getNamespace().asKubernetesNameSpace();

			kubernetesAccess.createNamespaceIfNotExistent(version);
			createSecretIfNotExistent(deployableVersion.getDockerRegistryId(), namespace);
			ensureServiceAccountIsPatchedWithRegistry(deployableVersion.getDockerRegistryId(), namespace);
			kubernetesAccess.deleteAllResourcesFromNameSpace(namespace, deployableVersion.getPrimaryLabel());

			final Set<HasMetadata> resources = getTemplateAsResources(deployableVersion);
			final List<HasMetadata> resourcesInNameSpace = kubernetesAccess.createResourcesInNameSpace(namespace, resources);
			final Deployable<WritableProjectVersion> deployable = this.updateDeployableWithCreatedResources(deployableVersion, resourcesInNameSpace);
			final Deployable<WritableProjectVersion> writableProjectVersionDeployable = this.updateDesiredStateOfDeployable(deployable, Deployed);
			final ReadableProject project = this.projectRepository.add(writableProjectVersionDeployable.getEntity().getProject());

			return project.getVersions().stream()
					.filter(projectVersion -> projectVersion.getUuid().equals(version.getId()))
					.findFirst()
					.orElse(null);
		} catch (KubernetesClientException e) {
			log.debug("Failed to deploy version {} of project {}", version.getName(), version.getProject().getName());
			throw e;
		}

		 */
		return null;
	}

	private <T extends Deployable<?>> T updateDeployableWithCreatedResources(T deployable, List<HasMetadata> createdResources) {
		/*
		ProjectVersion<?, ?> relatedVersion = deployable.getRelatedProjectVersion();
		updateDeploymentUrls(deployable, createdResources);
		deployable.setOutdated(false);
		if (!StringUtils.isEmpty(deployable.getDockerContentDigest())) {
			return deployable;
		}

		//it seems completely wrong, that this is happening here...
		return dockerRegistryClientFactory.getDockerRegistryClient(deployable.getRelatedProject())
				.map(client -> {
					final Manifest manifest = client.getManifest(relatedVersion);
					deployable.setDockerContentDigest(manifest.getDockerContentDigest());
					return deployable;
				}).orElse(null);

		 */
		return null;
	}

	private <T extends Deployable<?>> T updateDesiredStateOfDeployable(T deployable, DesiredState desiredState) {
		//deployable.setDesiredState(desiredState);
		//return deployable;
		return null;
	}

	private Set<HasMetadata> getTemplateAsResources(Deployable<?> deployable) {
		/*
		Set<HasMetadata> resources = new HashSet<>();
		for (DeployableConfigurationTemplate template : deployable.getConfigurationTemplates().getTemplates()) {
			final Map.Entry<String, String> primaryLabel = deployable.getPrimaryLabel();
			kubernetesAccess.loadResource(template.getContent()).stream()
					.peek(resource -> addLabelToResource(resource, primaryLabel.getKey(), primaryLabel.getValue()))
					.peek(resource -> addLabelToResource(resource, TEMPLATE_NAME, template.getName()))
					.forEach(resources::add);
		}
		return resources;

		 */
		return null;
	}

	private void addLabelToResource(HasMetadata resource, String key, String value) {
		/*
		if (resource.getMetadata() == null) {
			resource.setMetadata(new ObjectMeta());
		}
		if (resource instanceof Deployment) {
			Deployment deployment = (Deployment) resource;
			addLabelToMeta(deployment.getMetadata(), key, value);
			addLabelToMeta(deployment.getSpec().getTemplate().getMetadata(), key, value);
		} else if (resource instanceof StatefulSet) {
			StatefulSet statefulSet = (StatefulSet) resource;
			addLabelToMeta(statefulSet.getMetadata(), key, value);
			addLabelToMeta(statefulSet.getSpec().getTemplate().getMetadata(), key, value);
		} else {
			addLabelToMeta(resource.getMetadata(), key, value);
		}

		 */
	}

	private void updateDeploymentUrls(Deployable<?> deployable, List<HasMetadata> createdResources) {
		/*
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

		 */
	}

	private Secret createSecretIfNotExistent(DockerRegistry dockerRegistry, String namespace) throws JsonProcessingException {
		/*
		return kubernetesAccess.createImagePullSecretIfNotExistent(namespace,
				KubernetesConventions.secretName(dockerRegistry),
				dockerRegistry.getUserName(),
				dockerRegistry.getPassword(),
				dockerRegistry.getRegistryUrl());
	}

	private void createSecretIfNotExistent(UUID dockerRegistryId, String namespace) {
		this.dockerRegistryRepository.getById(dockerRegistryId)
				.ifPresent(dockerRegistry -> {
					try {
						this.createSecretIfNotExistent(dockerRegistry, namespace);
					} catch (JsonProcessingException e) {
						log.warn("Failed to create secret", e);
					}
				});

		 */
		return null;
	}

	private ServiceAccount ensureServiceAccountIsPatchedWithRegistry(DockerRegistry dockerRegistry, String namespace) {
		//return kubernetesAccess.patchServiceAccountIfNecessary(namespace, KubernetesConventions.secretName(dockerRegistry));
		return null;
	}

	private void ensureServiceAccountIsPatchedWithRegistry(UUID dockerRegistryId, String namespace) {
		//this.dockerRegistryRepository.getById(dockerRegistryId)
		//		.ifPresent(reg -> this.ensureServiceAccountIsPatchedWithRegistry(reg, namespace));
	}

	private boolean addLabelToMeta(ObjectMeta meta, String key, String value) {
		/*
		if (meta.getLabels() == null) {
			Map<String, String> labels = new HashMap<>();
			labels.put(key, value);
			meta.setLabels(labels);
			return true;
		}

		return meta.getLabels().put(key, value) == null;

		 */
		return false;
	}

	@Override
	public ReadableProjectVersion stopDeployment(WritableProjectVersion version) {
		/*
		try {
			log.debug("Stop deployment of version {} of project {}", version.getName(), version.getProject().getName());
			Deployable<WritableProjectVersion> deployableVersion = Deployables.of(version);
			kubernetesAccess.deleteNamespaceByLabel(deployableVersion.getPrimaryLabel());
			version.setUrls(Collections.emptyList());
			version.setDesiredState(NotDeployed);
			ReadableProject readableProject = projectRepository.add(version.getProject());
			return readableProject.getVersions().stream()
					.filter(projectVersion -> projectVersion.getUuid().equals(version.getId()))
					.findFirst().orElse(null);
		} catch (KubernetesClientException e) {
			log.debug("Failed to stop deployment of version {} of project {}", version.getName(), version.getProject().getName());
			throw e;
		}

		 */
		return null;
	}
}
