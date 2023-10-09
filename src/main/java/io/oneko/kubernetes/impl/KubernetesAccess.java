package io.oneko.kubernetes.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.Listable;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.oneko.event.EventDispatcher;
import io.oneko.kubernetes.NamespaceCreatedEvent;
import io.oneko.metrics.MetricNameBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.*;

import static net.logstash.logback.argument.StructuredArguments.kv;

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

	private final Timer createNamespaceTimer;
	private final Timer deleteNamespaceTimer;
	private final Timer createOrUpdateImagePullSecretTimer;
	private final Timer deleteImagePullSecretTimer;
	private final Timer patchServiceAccountTimer;

	public KubernetesAccess(@Value("${kubernetes.server.url:}") final String masterUrl,
							@Value("${kubernetes.auth.token:}") final String token,
							EventDispatcher eventDispatcher,
							MeterRegistry meterRegistry) {
		this.eventDispatcher = eventDispatcher;

		ConfigBuilder configBuilder = new ConfigBuilder();

		if (StringUtils.isNotBlank(masterUrl)) {
			configBuilder.withMasterUrl(masterUrl);
		}

		if (StringUtils.isNotBlank(token)) {
			configBuilder.withOauthToken(token);
		}

		Config config = configBuilder
			.build();

		kubernetesClient = new KubernetesClientBuilder().withConfig(config).build();

		createNamespaceTimer = timer("namespace", "create", meterRegistry);
		deleteNamespaceTimer = timer("namespace", "delete", meterRegistry);
		createOrUpdateImagePullSecretTimer = timer("imagePullSecret", "createOrUpdate", meterRegistry);
		deleteImagePullSecretTimer = timer("imagePullSecret", "delete", meterRegistry);
		patchServiceAccountTimer = timer("serviceAccount", "patch", meterRegistry);
	}

	private Timer timer(String resource, String action, MeterRegistry meterRegistry) {
		return Timer.builder(new MetricNameBuilder().durationOf("kubernetes.api.request").build())
			.publishPercentileHistogram()
			.tag("resource", resource)
			.tag("action", action)
			.register(meterRegistry);
	}

	Namespace createNamespaceIfNotExistent(String namespace) {
		final Timer.Sample sample = Timer.start();
		Namespace existingNamespace = kubernetesClient.namespaces().withName(namespace).get();
		if (existingNamespace != null) {
			return existingNamespace;
		}

		log.info("creating namespace ({})", kv("namespace", namespace));
		Namespace newNameSpace = new Namespace();
		ObjectMeta meta = new ObjectMeta();
		meta.setName(namespace);
		newNameSpace.setMetadata(meta);
		kubernetesClient.namespaces().create(newNameSpace);

		eventDispatcher.dispatch(new NamespaceCreatedEvent(newNameSpace.getMetadata().getName()));
		sample.stop(createNamespaceTimer);
		return newNameSpace;
	}

	Secret createOrUpdateImagePullSecretInNamespace(String namespace, String secretName, String userName, String password, String url) throws JsonProcessingException {
		final Timer.Sample sample = Timer.start();
		final Secret existingSecret = kubernetesClient.secrets()
			.inNamespace(namespace)
			.withName(secretName).get();

		if (existingSecret != null) {
			log.info("updating image pull secret ({}, {})", kv("image_pull_secret", secretName), kv("namespace", namespace));
		}

		Map<String, Object> dockerConfigMap = new HashMap<>();
		Map<String, Object> authsMap = new HashMap<>();
		Map<String, Object> authMap = new HashMap<>();
		authMap.put("username", userName);
		authMap.put("password", password);
		authsMap.put(url, authMap);
		dockerConfigMap.put("auths", authsMap);
		try {
			log.info("creating image pull secret ({}, {})", kv("image_pull_secret", secretName), kv("namespace", namespace));
			final String dockerConfigJson = new ObjectMapper().writeValueAsString(dockerConfigMap);
			final HashMap<String, String> dataMap = new HashMap<>();
			dataMap.put(".dockerconfigjson", new String(Base64.getEncoder().encode(dockerConfigJson.getBytes())));

			final Secret secret = kubernetesClient.secrets().inNamespace(namespace).createOrReplace(new SecretBuilder()
				.withApiVersion("v1")
				.withKind("Secret")
				.withData(dataMap)
				.withNewMetadata().withName(secretName).endMetadata()
				.withType("kubernetes.io/dockerconfigjson")
				.build());
			sample.stop(createOrUpdateImagePullSecretTimer);
			return secret;
		} catch (JsonProcessingException e) {
			log.error("failed to create image pull secret due to a JsonProcessingException.", e);
			throw e;
		}
	}

	void deleteImagePullSecretInNamespace(String namespace, String secretName) {
		deleteImagePullSecretTimer.record(() -> {
			kubernetesClient.secrets()
				.inNamespace(namespace)
				.withName(secretName)
				.delete();
		});
	}

	ServiceAccount addImagePullSecretToServiceAccountIfNecessary(String namespace, String imagePullSecretName) {
		return patchServiceAccountTimer.record(() -> {
			final ServiceAccount defaultServiceAccount = kubernetesClient.serviceAccounts()
				.inNamespace(namespace)
				.withName("default")
				.get();

			List<LocalObjectReference> imagePullSecrets = defaultServiceAccount.getImagePullSecrets();
			if (imagePullSecrets == null) {
				imagePullSecrets = new ArrayList<>();
			}

			if (imagePullSecrets.stream().anyMatch(ips -> ips.getName().equals(imagePullSecretName))) {
				return defaultServiceAccount;
			}

			log.info("adding image pull secret to default service account ({}, {})", kv("image_pull_secret", imagePullSecretName), kv("namespace", namespace));
			imagePullSecrets.add(new LocalObjectReference(imagePullSecretName));
			defaultServiceAccount.setImagePullSecrets(imagePullSecrets);
			return kubernetesClient.serviceAccounts()
				.inNamespace(namespace)
				.createOrReplace(defaultServiceAccount);
		});
	}

	ServiceAccount removeImagePullSecretFromServiceAccountIfNecessary(String namespace, String imagePullSecretName) {
		return patchServiceAccountTimer.record(() -> {
			final ServiceAccount defaultServiceAccount = kubernetesClient.serviceAccounts()
				.inNamespace(namespace)
				.withName("default")
				.get();

			List<LocalObjectReference> imagePullSecrets = defaultServiceAccount.getImagePullSecrets();
			if (imagePullSecrets == null) {
				return defaultServiceAccount;
			}

			if (imagePullSecrets.stream().noneMatch(ips -> ips.getName().equals(imagePullSecretName))) {
				return defaultServiceAccount;
			}

			imagePullSecrets.removeIf(ips -> ips.getName().equals(imagePullSecretName));

			log.info("removed image pull secret from default service account ({}, {})", kv("image_pull_secret", imagePullSecretName), kv("namespace", namespace));
			imagePullSecrets.add(new LocalObjectReference(imagePullSecretName));
			defaultServiceAccount.setImagePullSecrets(imagePullSecrets);
			return kubernetesClient.serviceAccounts()
				.inNamespace(namespace)
				.createOrReplace(defaultServiceAccount);
		});
	}

	public void deleteNamespaceByName(String name) {
		deleteNamespaceTimer.record(() -> {
			kubernetesClient.namespaces().withName(name).delete();
		});
	}

	public String getLogsForPodAndContainerInNamespace(String podName, String containerName, String namespace) {
		return kubernetesClient.pods().inNamespace(namespace).withName(podName).inContainer(containerName).getLog();
	}

	public LogWatch streamLogsForPodAndContainerInNamespace(String podName, String containerName, String namespace, OutputStream outputStream) {
		return kubernetesClient.pods().inNamespace(namespace).withName(podName).inContainer(containerName).watchLog(outputStream);
	}

	public List<Pod> getPodsForHelmReleaseInNamespace(String helmReleaseName, String namespace) {
		List<Deployment> deployments = kubernetesClient.apps()
			.deployments()
			.inNamespace(namespace)
			.list()
			.getItems()
			.stream().filter(dp -> dp.getMetadata().getAnnotations().containsKey("meta.helm.sh/release-name")
				&& dp.getMetadata().getAnnotations().get("meta.helm.sh/release-name").equals(helmReleaseName)).toList();

		List<Pod> deploymentPodList = deployments
			.stream().map(dp -> dp.getSpec().getSelector())
			.map(sel -> kubernetesClient.pods().inNamespace(namespace).withLabelSelector(sel))
			.map(Listable::list)
			.flatMap(pl -> pl.getItems().stream())
			.toList();

		List<StatefulSet> statefulSets = kubernetesClient.apps()
			.statefulSets()
			.inNamespace(namespace)
			.list()
			.getItems()
			.stream().filter(dp -> dp.getMetadata().getAnnotations().containsKey("meta.helm.sh/release-name")
				&& dp.getMetadata().getAnnotations().get("meta.helm.sh/release-name").equals(helmReleaseName)).toList();

		List<Pod> stsPodList = statefulSets
			.stream().map(dp -> dp.getSpec().getSelector())
			.map(sel -> kubernetesClient.pods().inNamespace(namespace).withLabelSelector(sel))
			.map(Listable::list)
			.flatMap(pl -> pl.getItems().stream())
			.toList();

		List<Pod> result = new ArrayList<>();
		result.addAll(deploymentPodList);
		result.addAll(stsPodList);

		return result;
	}
}
