package io.oneko.docker.persistence;

import static org.assertj.core.api.Assertions.*;

import io.oneko.event.CurrentEventTrigger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.oneko.docker.WritableDockerRegistry;
import io.oneko.event.EventDispatcher;

class DockerRegistryInMemoryRepositoryTest {

	private DockerRegistryInMemoryRepository uut;
	private EventDispatcher dispatcher;
	private CurrentEventTrigger currentEventTrigger;

	@BeforeEach
	public void setup() {
		currentEventTrigger = new CurrentEventTrigger();
		dispatcher = new EventDispatcher(currentEventTrigger);
		uut = new DockerRegistryInMemoryRepository(dispatcher);
	}

	@Test
	void testCrud() {
		assertThat(uut.getAll()).isEmpty();

		WritableDockerRegistry reg = new WritableDockerRegistry();
		reg.setName("myreg");

		uut.add(reg);
		assertThat(uut.getAll()).hasSize(1);

		uut.remove(reg);
		assertThat(uut.getAll()).isEmpty();
	}

	@Test
	void testGetByName() {
		WritableDockerRegistry reg = new WritableDockerRegistry();
		reg.setName("myreg");

		uut.add(reg);
		assertThat(uut.getAll()).hasSize(1);
		assertThat(uut.getByName("myreg")).isPresent();
	}

	@Test
	void testDontModifyRepoContent() {
		//TODO
	}
}
