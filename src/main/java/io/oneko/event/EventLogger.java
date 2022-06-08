package io.oneko.event;

import static net.logstash.logback.argument.StructuredArguments.kv;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventLogger {

	public EventLogger(EventDispatcher eventDispatcher) {
		if (log.isTraceEnabled()) {
			eventDispatcher.registerListener(this::logEvent);
		}
	}

	public void logEvent(Event event) {
		log.trace("{}", kv("event", event.humanReadable()));
	}
}
