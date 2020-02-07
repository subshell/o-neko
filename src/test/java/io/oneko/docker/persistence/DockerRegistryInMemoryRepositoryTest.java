package io.oneko.docker.persistence;

import io.oneko.docker.WritableDockerRegistry;
import io.oneko.event.EventDispatcher;
import org.junit.Before;
import org.junit.Test;
import reactor.test.StepVerifier;

public class DockerRegistryInMemoryRepositoryTest {

	private DockerRegistryInMemoryRepository uut;
	private EventDispatcher dispatcher;

	@Before
	public void setup() {
		dispatcher = new EventDispatcher();
		uut = new DockerRegistryInMemoryRepository(dispatcher);
	}

	@Test
	public void testCrud() {
		StepVerifier.create(uut.getAll())
				.expectNextCount(0)
				.verifyComplete();

		WritableDockerRegistry reg = new WritableDockerRegistry();
		reg.setName("myreg");

		StepVerifier.create(uut.add(reg))
				.expectNextCount(1)
				.verifyComplete();

		StepVerifier.create(uut.getAll())
				.expectNextCount(1)
				.verifyComplete();

		StepVerifier.create(uut.remove(reg))
				.verifyComplete();

		StepVerifier.create(uut.getAll())
				.expectNextCount(0)
				.verifyComplete();
	}

	@Test
	public void testGetByName() {
		WritableDockerRegistry reg = new WritableDockerRegistry();
		reg.setName("myreg");

		StepVerifier.create(uut.add(reg))
				.expectNextCount(1)
				.verifyComplete();

		StepVerifier.create(uut.getByName("myreg"))
				.expectNextCount(1)
				.verifyComplete();
	}

	@Test
	public void testDontModifyRepoContent() {
		//TODO
	}
}
