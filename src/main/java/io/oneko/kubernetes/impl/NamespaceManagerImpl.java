package io.oneko.kubernetes.impl;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.oneko.docker.DockerRegistry;
import io.oneko.docker.DockerRegistryRepository;
import io.oneko.kubernetes.KubernetesConventions;
import io.oneko.kubernetes.NamespaceManager;
import io.oneko.namespace.Namespace;
import io.oneko.namespace.NamespaceRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NamespaceManagerImpl implements NamespaceManager {

	private final KubernetesAccess kubernetesAccess;
	private final DockerRegistryRepository dockerRegistryRepository;
	private final NamespaceRepository namespaceRepository;

	public NamespaceManagerImpl(KubernetesAccess kubernetesAccess,
															DockerRegistryRepository dockerRegistryRepository,
															NamespaceRepository namespaceRepository) {
		this.kubernetesAccess = kubernetesAccess;
		this.dockerRegistryRepository = dockerRegistryRepository;
		this.namespaceRepository = namespaceRepository;
	}

	@Override
	public void createNamespaceAndAddImagePullSecrets(Namespace namespace) {
		final String namespaceName = namespace.asKubernetesNameSpace();
		kubernetesAccess.createNamespaceIfNotExistent(namespaceName);
		dockerRegistryRepository.getAll().forEach(registry -> createOrUpdateImagePullSecretInNamespace(namespaceName, registry));
	}

	private void createOrUpdateImagePullSecretInNamespace(String namespace, DockerRegistry registry) {
		try {
			final String secretName = KubernetesConventions.secretName(registry);
			kubernetesAccess.createOrUpdateImagePullSecretInNamespace(namespace, secretName, registry.getUserName(), registry.getPassword(), registry.getRegistryUrl());
			kubernetesAccess.addImagePullSecretToServiceAccountIfNecessary(namespace, secretName);
		} catch (JsonProcessingException e) {
			log.error("Failed to add image pull secret for registry {}", registry, e);
		}
	}

	@Override
	public void deleteNamespace(Namespace namespace) {
		kubernetesAccess.deleteNamespaceByName(namespace.asKubernetesNameSpace());
	}

	@Override
	public void updateImagePullSecretsWithRegistry(DockerRegistry dockerRegistry) {
		namespaceRepository.getAll().forEach(namespace -> createOrUpdateImagePullSecretInNamespace(namespace.asKubernetesNameSpace(), dockerRegistry));
	}

	@Override
	public void removeImagePullSecretsForRegistry(DockerRegistry dockerRegistry) {
		final String secretName = KubernetesConventions.secretName(dockerRegistry);
		namespaceRepository.getAll().forEach(namespace -> {
			final String namespaceName = namespace.asKubernetesNameSpace();
			kubernetesAccess.deleteImagePullSecretInNamespace(namespaceName, secretName);
			kubernetesAccess.removeImagePullSecretFromServiceAccountIfNecessary(namespaceName, secretName);
		});
	}
}
