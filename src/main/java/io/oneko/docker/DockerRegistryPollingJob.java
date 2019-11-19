package io.oneko.docker;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import lombok.Data;
import reactor.core.Disposable;

@Data
public class DockerRegistryPollingJob {
	private Clock clock = Clock.systemDefaultZone();
	private Duration timeoutDuration = Duration.ofMinutes(5);

	private Disposable job;
	private Instant startDate;


	public DockerRegistryPollingJob(Disposable job, Instant startDate) {
		this.job = job;
		this.startDate = startDate;
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
		job.dispose();
	}
}
