package io.oneko.kubernetes.impl;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.oneko.docker.DockerRegistryRepository;
import io.oneko.kubernetes.KubernetesConventions;
import io.oneko.kubernetes.NamespaceManager;
import io.oneko.namespace.DefinedNamespace;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NamespaceManagerImpl implements NamespaceManager {

	private final KubernetesAccess kubernetesAccess;
	private final DockerRegistryRepository dockerRegistryRepository;

	public NamespaceManagerImpl(KubernetesAccess kubernetesAccess, DockerRegistryRepository dockerRegistryRepository) {
		this.kubernetesAccess = kubernetesAccess;
		this.dockerRegistryRepository = dockerRegistryRepository;
	}

	@Override
	public void createNamespaceAndAddImagePullSecrets(DefinedNamespace namespace) {
		final String namespaceName = namespace.asKubernetesNameSpace();
		kubernetesAccess.createNamespaceIfNotExistent(namespaceName);
		dockerRegistryRepository.getAll().forEach(registry -> {
			try {
				final String secretName = KubernetesConventions.secretName(registry);
				kubernetesAccess.createImagePullSecretIfNotExistent(namespaceName, secretName, registry.getUserName(), registry.getPassword(), registry.getRegistryUrl());
				kubernetesAccess.patchServiceAccountIfNecessary(namespaceName, secretName);
			} catch (JsonProcessingException e) {
				log.error("Failed to add image pull secret for registry {}", registry, e);
			}
		});
	}

	@Override
	public void deleteNamespace(DefinedNamespace namespace) {
		kubernetesAccess.deleteNamespaceByName(namespace.asKubernetesNameSpace());
	}
}
