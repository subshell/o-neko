package io.oneko.kubernetes.impl;

import static io.oneko.kubernetes.deployments.DesiredState.*;
import static io.oneko.project.ProjectConstants.LabelNames.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.oneko.deployable.DeployableConfigurationTemplate;
import io.oneko.docker.DockerRegistry;
import io.oneko.docker.DockerRegistryRepository;
import io.oneko.docker.v2.DockerRegistryV2ClientFactory;
import io.oneko.docker.v2.model.manifest.Manifest;
import io.oneko.kubernetes.KubernetesConventions;
import io.oneko.kubernetes.KubernetesDeploymentManager;
import io.oneko.kubernetes.deployments.Deployable;
import io.oneko.kubernetes.deployments.Deployables;
import io.oneko.kubernetes.deployments.DesiredState;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ProjectVersion;
import io.oneko.project.ReadableProject;
import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.WritableProjectVersion;
import io.oneko.projectmesh.ProjectMeshRepository;
import io.oneko.projectmesh.ReadableMeshComponent;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableMeshComponent;
import io.oneko.projectmesh.WritableProjectMesh;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
class KubernetesDeploymentManagerImpl implements KubernetesDeploymentManager {

	private final KubernetesAccess kubernetesAccess;
	private final DockerRegistryV2ClientFactory dockerRegistryV2ClientFactory;
	private final DockerRegistryRepository dockerRegistryRepository;
	private final ProjectRepository projectRepository;
	private final ProjectMeshRepository projectMeshRepository;

	KubernetesDeploymentManagerImpl(KubernetesAccess kubernetesAccess, DockerRegistryV2ClientFactory dockerRegistryV2ClientFactory,
	                                DockerRegistryRepository dockerRegistryRepository, ProjectRepository projectRepository, ProjectMeshRepository projectMeshRepository) {
		this.kubernetesAccess = kubernetesAccess;
		this.dockerRegistryV2ClientFactory = dockerRegistryV2ClientFactory;
		this.dockerRegistryRepository = dockerRegistryRepository;
		this.projectRepository = projectRepository;
		this.projectMeshRepository = projectMeshRepository;
	}

	@Override
	public ReadableProjectVersion deploy(WritableProjectVersion version) {
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
	}

	@Override
	public ReadableProjectMesh deploy(WritableProjectMesh mesh) {
		log.debug("Deploying project mesh {}", mesh.getName());
		return this.deployComponentsOfMesh(mesh, mesh.getComponents());
	}

	@Override
	public ReadableMeshComponent deploy(WritableMeshComponent component) {
		log.debug("Deploying component {} of project mesh {}", component.getName(), component.getOwner().getName());
		final ReadableProjectMesh mesh = this.deployComponentsOfMesh(component.getOwner(), Collections.singletonList(component));
		return mesh.getComponentById(component.getId()).orElse(null);
	}

	private ReadableProjectMesh deployComponentsOfMesh(WritableProjectMesh mesh, Collection<WritableMeshComponent> components) {
		try {
			String namespace = mesh.getNamespace().asKubernetesNameSpace();
			kubernetesAccess.createNamespaceIfNotExistent(mesh);

			final List<Deployable<WritableMeshComponent>> deployableComponents = components.stream().map(Deployables::of).collect(Collectors.toList());

			deployableComponents.forEach(deployableComponent -> {
				createSecretIfNotExistent(deployableComponent.getDockerRegistryId(), namespace);
				ensureServiceAccountIsPatchedWithRegistry(deployableComponent.getDockerRegistryId(), namespace);
				kubernetesAccess.deleteAllResourcesFromNameSpace(namespace, deployableComponent.getPrimaryLabel());
			});

			final List<Deployable<WritableMeshComponent>> deployableMeshComponents = deployableComponents.stream()
					.map(deployableComponent -> {
						final Set<HasMetadata> resources = getTemplateAsResources(deployableComponent);
						final List<HasMetadata> resourcesInNameSpace = kubernetesAccess.createResourcesInNameSpace(namespace, resources);
						final Deployable<WritableMeshComponent> meshComponentDeployable = updateDeployableWithCreatedResources(deployableComponent, resourcesInNameSpace);
						return updateDesiredStateOfDeployable(meshComponentDeployable, Deployed);
					}).collect(Collectors.toList());

			if (deployableMeshComponents.isEmpty()) {
				return mesh.readable();
			}

			final WritableProjectMesh owner = deployableMeshComponents.get(0).getEntity().getOwner();
			return this.projectMeshRepository.add(owner);
		} catch (KubernetesClientException e) {
			log.debug("Failed to deploy project mesh {}", mesh.getName());
			throw e;
		}
	}

	private <T extends Deployable<?>> T updateDeployableWithCreatedResources(T deployable, List<HasMetadata> createdResources) {
		ProjectVersion<?, ?> relatedVersion = deployable.getRelatedProjectVersion();
		createdResources.forEach(hasMetadata -> setDeploymentUrlsTo(deployable, hasMetadata));
		deployable.setOutdated(false);
		if (!StringUtils.isEmpty(deployable.getDockerContentDigest())) {
			return deployable;
		}

		//it seems completely wrong, that this is happening here...
		return dockerRegistryV2ClientFactory.getDockerRegistryClient(deployable.getRelatedProject())
				.map(client -> {
					final Manifest manifest = client.getManifest(relatedVersion);
					deployable.setDockerContentDigest(manifest.getDockerContentDigest());
					return deployable;
				}).orElse(null);
	}

	private <T extends Deployable<?>> T updateDesiredStateOfDeployable(T deployable, DesiredState desiredState) {
		deployable.setDesiredState(desiredState);
		return deployable;
	}

	private Set<HasMetadata> getTemplateAsResources(Deployable<?> deployable) {
		Set<HasMetadata> resources = new HashSet<>();
		for (DeployableConfigurationTemplate template : deployable.getConfigurationTemplates().getTemplates()) {
			final Map.Entry<String, String> primaryLabel = deployable.getPrimaryLabel();
			kubernetesAccess.loadResource(template.getContent()).stream()
					.peek(resource -> addLabelToResource(resource, primaryLabel.getKey(), primaryLabel.getValue()))
					.peek(resource -> addLabelToResource(resource, TEMPLATE_NAME, template.getName()))
					.forEach(resources::add);
		}
		return resources;
	}

	private void addLabelToResource(HasMetadata resource, String key, String value) {
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
	}

	private void setDeploymentUrlsTo(Deployable<?> deployable, HasMetadata hasMetadata) {
		if (hasMetadata instanceof Ingress) {
			Ingress ingress = (Ingress) hasMetadata;

			List<String> urls = ingress
					.getSpec()
					.getRules()
					.stream()
					.map(IngressRule::getHost)
					.collect(Collectors.toList());
			log.trace("Found urls {} of {} {}", urls, deployable.getClass().getSimpleName(), deployable.getName());
			deployable.setUrls(urls);
		}
	}

	private Secret createSecretIfNotExistent(DockerRegistry dockerRegistry, String namespace) throws JsonProcessingException {
		return kubernetesAccess.createSecretIfNotExistent(namespace,
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
						// TODO ok?
					}
				});
	}

	private ServiceAccount ensureServiceAccountIsPatchedWithRegistry(DockerRegistry dockerRegistry, String namespace) {
		return kubernetesAccess.createServiceAccountIfNotExisting(namespace, KubernetesConventions.secretName(dockerRegistry));
	}

	private void ensureServiceAccountIsPatchedWithRegistry(UUID dockerRegistryId, String namespace) {
		this.dockerRegistryRepository.getById(dockerRegistryId)
				.ifPresent(reg -> this.ensureServiceAccountIsPatchedWithRegistry(reg, namespace));
	}

	private boolean addLabelToMeta(ObjectMeta meta, String key, String value) {
		if (meta.getLabels() == null) {
			Map<String, String> labels = new HashMap<>();
			labels.put(key, value);
			meta.setLabels(labels);
			return true;
		}

		return meta.getLabels().put(key, value) == null;
	}

	@Override
	public ReadableProjectVersion stopDeployment(WritableProjectVersion version) {
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
	}

	@Override
	public ReadableProjectMesh stopDeployment(WritableProjectMesh mesh) {
		try {
			log.debug("Stop deployment of mesh {}", mesh.getName());
			kubernetesAccess.deleteNamespaceByName(mesh.getNamespace().asKubernetesNameSpace());
			for (WritableMeshComponent component : mesh.getComponents()) {
				component.setUrls(Collections.emptyList());
				component.setDesiredState(NotDeployed);
			}
			return projectMeshRepository.add(mesh);
		} catch (KubernetesClientException e) {
			log.debug("Failed to stop deployment of mesh {}", mesh.getName());
			throw e;
		}
	}

	@Override
	public ReadableProjectMesh stopDeployment(WritableMeshComponent component) {
		try {
			log.debug("Stop deployment of component {} of mesh {}", component.getName(), component.getOwner().getName());
			Deployable<WritableMeshComponent> deployableComponent = Deployables.of(component);
			kubernetesAccess.deleteAllResourcesFromNameSpace(component.getOwner().getNamespace().asKubernetesNameSpace(), deployableComponent.getPrimaryLabel());
			component.setUrls(Collections.emptyList());
			component.setDesiredState(NotDeployed);
			return projectMeshRepository.add(component.getOwner());
		} catch (KubernetesClientException e) {
			log.debug("Failed to stop deployment of component {} of mesh {}", component.getName(), component.getOwner().getName());
			throw e;
		}
	}

}
