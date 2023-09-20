package io.oneko.kubernetes.impl;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.oneko.kubernetes.deployments.DeploymentRepository;
import io.oneko.kubernetes.deployments.ReadableDeployment;
import io.oneko.project.ReadableProjectVersion;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class KubernetesLogService {

	private final KubernetesAccess kubernetesAccess;
	private final DeploymentRepository deploymentRepository;


	public Map<String, List<String>> getPodAndContainersForVersion(ReadableProjectVersion version) {
		ReadableDeployment deployment = deploymentRepository.findByProjectVersionId(version.getId()).orElseThrow();
		List<Pod> pods = deployment.getReleaseNames().stream().map(release -> kubernetesAccess.getPodsForHelmReleaseInNamespace(release, version.getNamespaceOrElseFromProject()))
			.flatMap(List::stream)
			.toList();
		return pods.stream().collect(Collectors.toMap(pod -> pod.getMetadata().getName(), this::getContainerNamesForPod));
	}

	private List<String> getContainerNamesForPod(Pod pod) {
		List<String> initContainerNames = pod.getStatus().getInitContainerStatuses().stream().map(ContainerStatus::getName).toList();
		List<String> containerNames = pod.getStatus().getContainerStatuses().stream().map(ContainerStatus::getName).toList();
		List<String> ephemeralContainerNames = pod.getStatus().getEphemeralContainerStatuses().stream().map(ContainerStatus::getName).toList();
		List<String> result = new ArrayList<>();
		result.addAll(initContainerNames);
		result.addAll(containerNames);
		result.addAll(ephemeralContainerNames);
		return result;
	}

	public String getLogs(ReadableProjectVersion version, String pod, String container) {
		return kubernetesAccess.getLogsForPodAndContainerInNamespace(pod, container, version.getNamespaceOrElseFromProject());
	}

	public LogWatch streamLogs(ReadableProjectVersion version, String pod, String container, OutputStream stream) {
		return kubernetesAccess.streamLogsForPodAndContainerInNamespace(pod, container, version.getNamespaceOrElseFromProject(), stream);
	}
}
