package io.oneko.docker;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Data
@Slf4j
public class DockerRegistryPollingJob {
	private Clock clock = Clock.systemDefaultZone();
	private Duration timeoutDuration = Duration.ofMinutes(5);

	private Disposable job;
	private Instant startDate;

	public DockerRegistryPollingJob(Disposable job) {
		this.job = job;
		this.startDate = Instant.now();
	}

	public DockerRegistryPollingJob withTimeoutDuration(Duration timeoutDuration) {
		if (timeoutDuration != null) {
			this.timeoutDuration = timeoutDuration;
		}
		return this;
	}

	public boolean shouldCancel() {
		return startDate != null && clock.instant().minus(timeoutDuration).isAfter(startDate);
	}

	public boolean isCancelled() {
		return job.isDisposed();
	}

	public void cancel() {
		log.warn("Dispose docker registry polling job");
		job.dispose();
	}
}
