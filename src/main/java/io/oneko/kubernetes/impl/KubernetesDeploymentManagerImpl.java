package io.oneko.kubernetes.impl;

import static io.oneko.kubernetes.deployments.DesiredState.*;
import static io.oneko.project.ProjectConstants.LabelNames.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.oneko.docker.DockerRegistryRepository;
import io.oneko.project.*;
import io.oneko.projectmesh.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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
import io.oneko.docker.v2.DockerRegistryV2ClientFactory;
import io.oneko.kubernetes.KubernetesConventions;
import io.oneko.kubernetes.KubernetesDeploymentManager;
import io.oneko.kubernetes.deployments.Deployable;
import io.oneko.kubernetes.deployments.Deployables;
import io.oneko.kubernetes.deployments.DesiredState;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
class KubernetesDeploymentManagerImpl implements KubernetesDeploymentManager {

	private final KubernetesAccess kubernetesAccess;
	private final DockerRegistryV2ClientFactory dockerRegistryV2ClientFactory;
	private final ProjectRepository projectRepository;
	private final ProjectMeshRepository projectMeshRepository;
	private final DockerRegistryRepository dockerRegistryRepository;

	KubernetesDeploymentManagerImpl(KubernetesAccess kubernetesAccess, DockerRegistryV2ClientFactory dockerRegistryV2ClientFactory,
									ProjectRepository projectRepository, ProjectMeshRepository projectMeshRepository, DockerRegistryRepository dockerRegistryRepository) {
		this.kubernetesAccess = kubernetesAccess;
		this.dockerRegistryV2ClientFactory = dockerRegistryV2ClientFactory;
		this.projectRepository = projectRepository;
		this.projectMeshRepository = projectMeshRepository;
		this.dockerRegistryRepository = dockerRegistryRepository;
	}

	@Override
	public Mono<ReadableProjectVersion> deploy(WritableProjectVersion version) {
		try {
			log.debug("Deploying version {} of project {}", version.getName(), version.getProject().getName());
			Deployable<WritableProjectVersion> deployableVersion = Deployables.of(version);
			String namespace = version.getNamespace().asKubernetesNameSpace();
			return kubernetesAccess.createNamespaceIfNotExistent(version)
					.then(createSecretIfNotExistent(deployableVersion.getDockerRegistryId(), namespace))
					.then(ensureServiceAccountIsPatchedWithRegistry(deployableVersion.getDockerRegistryId(), namespace))
					.doOnNext(v -> kubernetesAccess.deleteAllResourcesFromNameSpace(namespace, deployableVersion.getPrimaryLabel()))
					.then(getTemplateAsResources(deployableVersion))
					.map(resources -> kubernetesAccess.createResourcesInNameSpace(namespace, resources))
					.flatMap(resources -> this.updateDeployableWithCreatedResources(deployableVersion, resources))
					.flatMap(deployable -> this.updateDesiredStateOfDeployable(deployable, Deployed))
					.flatMap(v -> this.projectRepository.add((WritableProject) v.getRelatedProject()))
					.map(project -> project.getVersionByUUID(version.getId()).get());
		} catch (KubernetesClientException e) {
			log.debug("Failed to deploy version {} of project {}", version.getName(), version.getProject().getName());
			return Mono.error(e);
		}
	}

	@Override
	public Mono<ReadableProjectMesh> deploy(WritableProjectMesh mesh) {
		log.debug("Deploying project mesh {}", mesh.getName());
		return this.deployComponentsOfMesh(mesh, mesh.getComponents());
	}

	@Override
	public Mono<ReadableMeshComponent> deploy(WritableMeshComponent component) {
		log.debug("Deploying component {} of project mesh {}", component.getName(), component.getOwner().getName());
		return this.deployComponentsOfMesh(component.getOwner(), Collections.singletonList(component))
				.map(mesh -> mesh.getComponentById(component.getId()))
				.map(Optional::get);
	}

	private Mono<ReadableProjectMesh> deployComponentsOfMesh(WritableProjectMesh mesh, Collection<WritableMeshComponent> components) {
		try {
			String namespace = mesh.getNamespace().asKubernetesNameSpace();
			return kubernetesAccess.createNamespaceIfNotExistent(mesh)
					.thenMany(Flux.fromIterable(components))
					.map(Deployables::of)
					.flatMap(deployableComponent -> createSecretIfNotExistent(deployableComponent.getDockerRegistryId(), namespace).thenReturn(deployableComponent))
					.flatMap(deployableComponent -> ensureServiceAccountIsPatchedWithRegistry(deployableComponent.getDockerRegistryId(), namespace).thenReturn(deployableComponent))
					.doOnNext(deployableComponent -> kubernetesAccess.deleteAllResourcesFromNameSpace(namespace, deployableComponent.getPrimaryLabel()))
					.flatMap(deployableComponent ->
							getTemplateAsResources(deployableComponent)
									.map(resources -> kubernetesAccess.createResourcesInNameSpace(namespace, resources))
									.flatMap(resources -> updateDeployableWithCreatedResources(deployableComponent, resources)))
					.flatMap(deployable -> updateDesiredStateOfDeployable(deployable, Deployed))
					.collectList()
					.filter(list -> !list.isEmpty())
					.map(deployables -> deployables.get(0).getEntity().getOwner())
					.flatMap(this.projectMeshRepository::add)
					.switchIfEmpty(Mono.just(mesh.readable()));
		} catch (KubernetesClientException e) {
			log.debug("Failed to deploy project mesh {}", mesh.getName());
			return Mono.error(e);
		}
	}

	private <T, D extends Deployable<T>> Mono<D> updateDeployableWithCreatedResources(D deployable, List<HasMetadata> createdResources) {
		ProjectVersion relatedVersion = deployable.getRelatedProjectVersion();
		updateDeploymentUrls(deployable, createdResources);
		deployable.setOutdated(false);
		if (StringUtils.isEmpty(deployable.getDockerContentDigest())) {
			//it seems completely wrong, that this is happening here...
			return dockerRegistryV2ClientFactory.getDockerRegistryClient(deployable.getRelatedProject())
					.flatMap(client -> client.getManifest(relatedVersion)
							.map(manifest -> {
								deployable.setDockerContentDigest(manifest.getDockerContentDigest());
								return deployable;
							}));
		} else {
			return Mono.just(deployable);
		}
	}

	private <T, D extends Deployable<T>> Mono<D> updateDesiredStateOfDeployable(D deployable, DesiredState desiredState) {
		deployable.setDesiredState(desiredState);
		return Mono.just(deployable);
	}

	private Mono<Set<HasMetadata>> getTemplateAsResources(Deployable<?> deployable) {
		Set<HasMetadata> resources = new HashSet<>();
		for (DeployableConfigurationTemplate template : deployable.getConfigurationTemplates().getTemplates()) {
			final Map.Entry<String, String> primaryLabel = deployable.getPrimaryLabel();
			kubernetesAccess.loadResource(template.getContent()).stream()
					.peek(resource -> addLabelToResource(resource, primaryLabel.getKey(), primaryLabel.getValue()))
					.peek(resource -> addLabelToResource(resource, TEMPLATE_NAME, template.getName()))
					.forEach(resources::add);
		}
		return Mono.just(resources);
	}

	private void addLabelToResource(HasMetadata resource, String key, String value) {
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
	}

	private void updateDeploymentUrls(Deployable<?> deployable, List<HasMetadata> createdResources) {
		List<String> urls = createdResources.stream()
				.flatMap(hasMetadata -> {
					if (hasMetadata instanceof Ingress) {
						var ingress = (Ingress) hasMetadata;
						return ingress
								.getSpec()
								.getRules()
								.stream()
								.map(IngressRule::getHost);
					} else if (hasMetadata instanceof io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress) { // can't wait to repeat this code again for networking/v1
						var ingress = (io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress) hasMetadata;
						return ingress
								.getSpec()
								.getRules()
								.stream()
								.map(io.fabric8.kubernetes.api.model.networking.v1beta1.IngressRule::getHost);
					}
					return Stream.empty();
				}).collect(Collectors.toList());

		log.trace("Found urls {} of {} {}", urls, deployable.getClass().getSimpleName(), deployable.getName());
		deployable.setUrls(urls);
	}

	private Mono<Secret> createSecretIfNotExistent(UUID dockerRegistryId, String namespace) {
		return this.dockerRegistryRepository.getById(dockerRegistryId)
				.flatMap(reg -> this.createSecretIfNotExistent(reg, namespace));
	}

	private Mono<Secret> createSecretIfNotExistent(DockerRegistry dockerRegistry, String namespace) {
		return kubernetesAccess.createSecretIfNotExistent(namespace,
				KubernetesConventions.secretName(dockerRegistry),
				dockerRegistry.getUserName(),
				dockerRegistry.getPassword(),
				dockerRegistry.getRegistryUrl());
	}

	private Mono<ServiceAccount> ensureServiceAccountIsPatchedWithRegistry(UUID dockerRegistryId, String namespace) {
		return this.dockerRegistryRepository.getById(dockerRegistryId)
				.flatMap(reg -> this.ensureServiceAccountIsPatchedWithRegistry(reg, namespace));
	}

	private Mono<ServiceAccount> ensureServiceAccountIsPatchedWithRegistry(DockerRegistry dockerRegistry, String namespace) {
		return kubernetesAccess.createServiceAccountIfNotExisting(namespace, KubernetesConventions.secretName(dockerRegistry));
	}

	private boolean addLabelToMeta(ObjectMeta meta, String key, String value) {
		if (meta.getLabels() == null) {
			Map<String, String> labels = new HashMap<>();
			labels.put(key, value);
			meta.setLabels(labels);
			return true;
		} else {
			return meta.getLabels().put(key, value) == null;
		}
	}

	@Override
	public Mono<ReadableProjectVersion> stopDeployment(WritableProjectVersion version) {
		try {
			log.debug("Stop deployment of version {} of project {}", version.getName(), version.getProject().getName());
			Deployable<WritableProjectVersion> deployableVersion = Deployables.of(version);
			kubernetesAccess.deleteNamespaceByLabel(deployableVersion.getPrimaryLabel());
			version.setUrls(Collections.emptyList());
			version.setDesiredState(NotDeployed);
			return projectRepository.add(version.getProject())
					.map(project -> project.getVersionByUUID(version.getId()))
					.filter(Optional::isPresent)
					.map(Optional::get);
		} catch (KubernetesClientException e) {
			log.debug("Failed to stop deployment of version {} of project {}", version.getName(), version.getProject().getName());
			return Mono.error(e);
		}
	}

	@Override
	public Mono<ReadableProjectMesh> stopDeployment(WritableProjectMesh mesh) {
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
			return Mono.error(e);
		}
	}

	@Override
	public Mono<ReadableProjectMesh> stopDeployment(WritableMeshComponent component) {
		try {
			log.debug("Stop deployment of component {} of mesh {}", component.getName(), component.getOwner().getName());
			Deployable<WritableMeshComponent> deployableComponent = Deployables.of(component);
			kubernetesAccess.deleteAllResourcesFromNameSpace(component.getOwner().getNamespace().asKubernetesNameSpace(), deployableComponent.getPrimaryLabel());
			component.setUrls(Collections.emptyList());
			component.setDesiredState(NotDeployed);
			return projectMeshRepository.add(component.getOwner());
		} catch (KubernetesClientException e) {
			log.debug("Failed to stop deployment of component {} of mesh {}", component.getName(), component.getOwner().getName());
			return Mono.error(e);
		}
	}

}
