package io.oneko.kubernetes.impl;

import static io.oneko.project.ProjectConstants.TemplateVariablesNames.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Deletable;
import io.oneko.event.EventDispatcher;
import io.oneko.kubernetes.NamespaceCreatedEvent;
import io.oneko.namespace.HasNamespace;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Hides away most parts of the kubernetes API's overall weirdness.
 * <p>
 * This class just wraps away the kubernetes client and should not interact with o-neko own entities.
 */
@Component
@Slf4j
public class KubernetesAccess {

	private final KubernetesClient kubernetesClient;
	private final EventDispatcher eventDispatcher;

	public KubernetesAccess(@Value("${kubernetes.server.url:}") final String masterUrl,
	                        @Value("${kubernetes.auth.token:}") final String token,
	                        EventDispatcher eventDispatcher) {
		this.eventDispatcher = eventDispatcher;

		ConfigBuilder configBuilder = new ConfigBuilder();

		if (StringUtils.isNotBlank(masterUrl)) {
			configBuilder.withMasterUrl(masterUrl);
		}

		if (StringUtils.isNotBlank(token)) {
			configBuilder.withOauthToken(token);
		}

		Config config = configBuilder
				.withNamespace("default")
				.build();

		kubernetesClient = new DefaultKubernetesClient(config);
	}

	List<Pod> getPodsByLabelInNameSpace(String nameSpace, Map.Entry<String, String> label) {
		return kubernetesClient.pods()
				.inNamespace(nameSpace)
				.withLabel(label.getKey(), label.getValue())
				.list()
				.getItems();
	}

	List<HasMetadata> getAllResourcesInNamespaceWithLabel(String namespace, String key, String value) {
		List<HasMetadata> result = new ArrayList<>();

		result.addAll(kubernetesClient.apps().deployments().inNamespace(namespace).withLabel(key, value).list().getItems());
		result.addAll(kubernetesClient.apps().statefulSets().inNamespace(namespace).withLabel(key, value).list().getItems());
		result.addAll(kubernetesClient.apps().replicaSets().inNamespace(namespace).withLabel(key, value).list().getItems());
		result.addAll(kubernetesClient.pods().inNamespace(namespace).withLabel(key, value).list().getItems());
		result.addAll(kubernetesClient.services().inNamespace(namespace).withLabel(key, value).list().getItems());
		result.addAll(kubernetesClient.extensions().ingresses().inNamespace(namespace).withLabel(key, value).list().getItems());
		result.addAll(kubernetesClient.configMaps().inNamespace(namespace).withLabel(key, value).list().getItems());
		result.addAll(kubernetesClient.persistentVolumeClaims().inNamespace(namespace).withLabel(key, value).list().getItems());

		return result;
	}

	void deleteAllResourcesFromNameSpace(String nameSpace, Map.Entry<String, String> label) {
		final Deletable<Boolean> deletables = kubernetesClient.resourceList(getAllResourcesInNamespaceWithLabel(nameSpace, label.getKey(), label.getValue())).cascading(true);
		deletables.delete();
	}

	List<HasMetadata> createResourcesInNameSpace(String namespace, Collection<HasMetadata> resources) {
		return kubernetesClient.resourceList(resources)
				.inNamespace(namespace)
				.createOrReplace();
	}

	Namespace createNamespaceIfNotExistent(HasNamespace hasNamespace) {
		final String namespace = hasNamespace.getNamespace().asKubernetesNameSpace();
		Namespace existingNamespace = kubernetesClient.namespaces().withName(namespace).get();
		if (existingNamespace != null) {
			return existingNamespace;
		}

		log.info("Creating namespace with name {} for {} {}", namespace, hasNamespace.getClass().getSimpleName(), hasNamespace.getId());
		Namespace newNameSpace = new Namespace();
		ObjectMeta meta = new ObjectMeta();
		meta.setName(namespace);
		meta.setLabels(hasNamespace.getNamespaceLabels());
		newNameSpace.setMetadata(meta);
		kubernetesClient.namespaces().create(newNameSpace);

		eventDispatcher.createAndDispatchEvent(Mono.just(newNameSpace), (ns, trigger) -> new NamespaceCreatedEvent(ns.getMetadata().getName(), trigger)); // TODO

		return newNameSpace;
	}

	Secret createSecretIfNotExistent(String namespace, String secretName, String userName, String password, String url) throws JsonProcessingException {
		final Secret existingSecret = kubernetesClient.secrets()
				.inNamespace(namespace)
				.withName(secretName).get();

		if (existingSecret != null) {
			return existingSecret;
		}

		Map<String, Object> dockerConfigMap = new HashMap<>();
		Map<String, Object> authsMap = new HashMap<>();
		Map<String, Object> authMap = new HashMap<>();
		authMap.put("username", userName);
		authMap.put("password", password);
		authsMap.put(url, authMap);
		dockerConfigMap.put("auths", authsMap);
		try {
			log.info("Creating secret for with name {} to namespace with name {}", secretName, namespace);
			final String dockerConfigJson = new ObjectMapper().writeValueAsString(dockerConfigMap);
			final HashMap<String, String> dataMap = new HashMap<>();
			dataMap.put(".dockerconfigjson", new String(Base64.getEncoder().encode(dockerConfigJson.getBytes())));

			return kubernetesClient
					.secrets()
					.inNamespace(namespace)
					.createNew()
					.withApiVersion("v1")
					.withKind("Secret")
					.withData(dataMap)
					.withNewMetadata().withName(secretName)
					.endMetadata()
					.withType("kubernetes.io/dockerconfigjson")
					.done();
		} catch (JsonProcessingException e) {
			log.error("Failed to create docker registry secret due to a JsonProcessingException.", e);
			throw e;
		}
	}

	ServiceAccount createServiceAccountIfNotExisting(String namespace, String accountName) {
		final ServiceAccount defaultServiceAccount = kubernetesClient.serviceAccounts()
				.inNamespace(namespace)
				.withName("default")
				.get();

		List<LocalObjectReference> imagePullSecrets = defaultServiceAccount.getImagePullSecrets();
		if (imagePullSecrets == null) {
			imagePullSecrets = new ArrayList<>();
		}

		if (imagePullSecrets.stream().anyMatch(ips -> ips.getName().equals(accountName))) {
			return defaultServiceAccount;
		}

		log.info("Adding ImagePullSecret with name {} to default service account in namespace {}", accountName, namespace);
		imagePullSecrets.add(new LocalObjectReference(accountName));
		defaultServiceAccount.setImagePullSecrets(imagePullSecrets);
		return kubernetesClient.serviceAccounts()
				.inNamespace(namespace)
				.createOrReplace(defaultServiceAccount);
	}

	List<HasMetadata> loadResource(String staticContent) {
		try (InputStream is = new ByteArrayInputStream(staticContent.getBytes())) {
			return kubernetesClient.load(is).get();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteNamespacesWithProjectId(String projectId) {
		this.deleteNamespaceByLabel(ONEKO_PROJECT, projectId);
	}

	public void deleteNamespaceWithProjectVersionId(String versionId) {
		this.deleteNamespaceByLabel(ONEKO_VERSION, versionId);
	}

	public void deleteNamespaceByLabel(String key, String value) {
		kubernetesClient.namespaces().withLabel(key, value).delete();
	}

	public void deleteNamespaceByLabel(Map.Entry<String, String> label) {
		kubernetesClient.namespaces().withLabel(label.getKey(), label.getValue()).delete();
	}

	public void deleteNamespaceByName(String name) {
		kubernetesClient.namespaces().withName(name).delete();
	}

}
