package io.oneko.docker.persistence;

import io.oneko.Profiles;
import io.oneko.docker.DockerRegistry;
import io.oneko.docker.ReadableDockerRegistry;
import io.oneko.docker.WritableDockerRegistry;
import io.oneko.docker.event.EventAwareDockerRegistryRepository;
import io.oneko.event.EventDispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@Profile(Profiles.IN_MEMORY)
public class DockerRegistryInMemoryRepository extends EventAwareDockerRegistryRepository {

	private final Map<UUID, ReadableDockerRegistry> innerRepository = new HashMap<>();

	@Autowired
	DockerRegistryInMemoryRepository(EventDispatcher eventDispatcher) {
		super(eventDispatcher);
	}

	@Override
	protected Mono<ReadableDockerRegistry> addInternally(WritableDockerRegistry dockerRegistry) {
		final ReadableDockerRegistry readable = dockerRegistry.readable();
		innerRepository.put(dockerRegistry.getId(), readable);
		return Mono.just(readable);
	}

	@Override
	protected Mono<Void> removeInternally(DockerRegistry dockerRegistry) {
		innerRepository.remove(dockerRegistry.getUuid());
		return Mono.empty();
	}

	@Override
	public Mono<ReadableDockerRegistry> getById(UUID registryId) {
		return Mono.justOrEmpty(innerRepository.get(registryId));
	}

	@Override
	public Mono<ReadableDockerRegistry> getByName(String registryName) {
		return Mono.justOrEmpty(innerRepository.values().stream()
				.filter(registry -> registry.getName().equals(registryName))
				.findFirst());
	}

	@Override
	public Flux<ReadableDockerRegistry> getAll() {
		return Flux.fromIterable(innerRepository.values());
	}
}
