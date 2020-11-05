package io.oneko.docker;


import static org.assertj.core.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import io.oneko.utils.TimeMachine;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

class DockerRegistryPollingJobTest {

	@Test
	void testJobShouldTimeout() {
		TimeMachine timeMachine = new TimeMachine();
		DockerRegistryPollingJob uut = new DockerRegistryPollingJob(Flux.empty().subscribe()).withTimeoutDuration(Duration.ofMinutes(5));
		uut.setClock(timeMachine);
		assertThat(uut.shouldCancel()).isFalse();

		timeMachine.timeTravelTo(timeMachine.instant().plus(Duration.ofMinutes(6)));
		assertThat(uut.shouldCancel()).isTrue();
	}

	@Test
	void shouldBeDisposedIfUnderlyingStreamIsDisposed() {
		EmitterProcessor flux = EmitterProcessor.create();

		DockerRegistryPollingJob uut = new DockerRegistryPollingJob(flux.subscribe());
		assertThat(uut.isCancelled()).isFalse();

		flux.onComplete();
		assertThat(uut.isCancelled()).isTrue();
	}

	@Test
	void shouldBeCancellable() {
		EmitterProcessor flux = EmitterProcessor.create();

		DockerRegistryPollingJob uut = new DockerRegistryPollingJob(flux.subscribe());
		assertThat(uut.isCancelled()).isFalse();

		uut.cancel();
		assertThat(uut.isCancelled()).isTrue();
	}
}
