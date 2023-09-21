package io.oneko.kubernetes;

import io.marioslab.basis.template.Template;
import io.marioslab.basis.template.TemplateContext;
import io.marioslab.basis.template.TemplateLoader;
import io.oneko.configuration.Controllers;
import io.oneko.kubernetes.deployments.PodAndContainerDTO;
import io.oneko.kubernetes.impl.KubernetesLogService;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ReadableProject;
import io.oneko.project.ReadableProjectVersion;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(LogsController.PATH)
@Slf4j
@AllArgsConstructor
public class LogsController {

	public static final String PATH = Controllers.ROOT_PATH + "/logs";

	private final KubernetesLogService logService;
	private final ProjectRepository projectRepository;
	private final ExternalLoggingProperties loggingProperties;

	@GetMapping("/containers/project/{projectId}/version/{projectVersionId}")
	public List<PodAndContainerDTO> getPodsAndContainersForProjectVersion(@PathVariable UUID projectId, @PathVariable UUID projectVersionId) {
		ReadableProject readableProject = projectRepository.getById(projectId).orElseThrow();
		ReadableProjectVersion readableProjectVersion = readableProject.getVersionById(projectVersionId).orElseThrow();
		return logService.getPodAndContainersForVersion(readableProjectVersion).entrySet()
			.stream()
			.map(entry -> PodAndContainerDTO.build(entry.getKey(), entry.getValue(), externalLogsUrlMappingFunction(entry.getKey(), readableProjectVersion.getNamespaceOrElseFromProject())))
			.collect(Collectors.toList());
	}

	private Function<String, String> externalLogsUrlMappingFunction(String podName, String namespace) {
		TemplateLoader.MapTemplateLoader loader = new TemplateLoader.MapTemplateLoader();
		loader.set("tpl", loggingProperties.getExternalLogUrlTemplate());
		final Template tpl = loader.load("tpl");

		TemplateContext context = new TemplateContext();
		context.set("pod", podName);
		context.set("namespace", namespace);
		return containerName -> {
			context.set("container", containerName);
			return tpl.render(context);
		};
	}

	@GetMapping("/project/{projectId}/version/{projectVersionId}")
	public String getLogsForPodAndContainer(@PathVariable UUID projectId, @PathVariable UUID projectVersionId, @RequestParam(value = "pod", required = true) String pod, @RequestParam(value = "container", required = false) String container) {
		ReadableProject readableProject = projectRepository.getById(projectId).orElseThrow();
		ReadableProjectVersion readableProjectVersion = readableProject.getVersionById(projectVersionId).orElseThrow();
		return logService.getLogs(readableProjectVersion, pod, container);
	}
}
