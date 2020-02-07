package io.oneko.docker.persistence;

import io.oneko.Profiles;
import io.oneko.docker.DockerRegistry;
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

	private final Map<UUID, DockerRegistry> innerRepository = new HashMap<>();

	@Autowired
	DockerRegistryInMemoryRepository(EventDispatcher eventDispatcher) {
		super(eventDispatcher);
	}

	@Override
	protected Mono<DockerRegistry> addInternally(WritableDockerRegistry dockerRegistry) {
		final DockerRegistry readable = dockerRegistry.readable();
		innerRepository.put(dockerRegistry.getId(), dockerRegistry);
		return Mono.just(readable);
	}

	@Override
	protected Mono<Void> removeInternally(DockerRegistry dockerRegistry) {
		innerRepository.remove(dockerRegistry.getId());
		return Mono.empty();
	}

	@Override
	public Mono<DockerRegistry> getById(UUID registryId) {
		return Mono.justOrEmpty(innerRepository.get(registryId));
	}

	@Override
	public Mono<DockerRegistry> getByName(String registryName) {
		final Optional<DockerRegistry> match = innerRepository.values().stream()
				.filter(registry -> registry.getName().equals(registryName))
				.findFirst();
		return Mono.justOrEmpty(match);
	}

	@Override
	public Flux<DockerRegistry> getAll() {
		return Flux.fromIterable(innerRepository.values());
	}
}
