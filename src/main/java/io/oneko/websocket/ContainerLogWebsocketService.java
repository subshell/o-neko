package io.oneko.websocket;

import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.oneko.kubernetes.impl.KubernetesLogService;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ReadableProject;
import io.oneko.project.ReadableProjectVersion;
import io.oneko.websocket.message.LogsMessage;
import io.oneko.websocket.message.ONekoWebSocketMessage;
import io.oneko.websocket.message.SubscribeToLogsMessage;
import io.oneko.websocket.message.UnsubscribeFromLogsMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.oneko.util.MoreStructuredArguments.projectKv;
import static io.oneko.util.MoreStructuredArguments.versionKv;
import static net.logstash.logback.argument.StructuredArguments.kv;

@Service
@Slf4j
public class ContainerLogWebsocketService implements WebsocketListener {

	private final KubernetesLogService logService;
	private final SessionWebSocketHandler webSocketHandler;
	private final ProjectRepository projectRepository;
	private final Map<String, LogWatch> sessionIdToLogWatch = new ConcurrentHashMap<>();

	public ContainerLogWebsocketService(KubernetesLogService logService, SessionWebSocketHandler webSocketHandler, ProjectRepository projectRepository) {
		this.logService = logService;
		this.webSocketHandler = webSocketHandler;
		this.projectRepository = projectRepository;
		webSocketHandler.registerListener(this);
	}

	@Override
	public void onMessage(ONekoWebSocketMessage message, String sessionId) {
		if (message instanceof SubscribeToLogsMessage) {
			subscribe((SubscribeToLogsMessage) message, sessionId);
		} else if (message instanceof UnsubscribeFromLogsMessage) {
			unsubscribe(sessionId);
		}
	}

	@Override
	public void sessionClosed(String sessionId) {
		unsubscribe(sessionId);
	}

	private void subscribe(SubscribeToLogsMessage message, String sessionId) {
		ReadableProject project = projectRepository.getById(message.getProjectId()).orElseThrow();
		ReadableProjectVersion version = project.getVersionById(message.getVersionId()).orElseThrow();
		log.info("starting log stream ({}, {}, {}, {}, {})", kv("sessionId", sessionId), projectKv(project), versionKv(version), kv("pod", message.getPod()), kv("container", message.getContainer()));
		LogWatch logWatch = logService.streamLogs(version, message.getPod(), message.getContainer(), new ObservableStringLineOutputStream(logs -> {
			List<String> lines = logs.lines().toList();
			LogsMessage response = new LogsMessage(new Date(), lines);
			webSocketHandler.send(sessionId, response);
		}));
		LogWatch previousValue = sessionIdToLogWatch.put(sessionId, logWatch);
		if (previousValue != null) {
			log.info("closing previously registered log stream ({})", kv("sessionId", sessionId));
			previousValue.close();
		}
	}

	private void unsubscribe(String sessionId) {
		LogWatch logWatch = sessionIdToLogWatch.remove(sessionId);
		if (logWatch != null) {
			log.info("removing log stream ({})", kv("sessionId", sessionId));
			logWatch.close();
		}
	}
}
