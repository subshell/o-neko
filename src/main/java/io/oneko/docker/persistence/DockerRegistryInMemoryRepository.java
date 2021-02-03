package io.oneko.docker.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.docker.DockerRegistry;
import io.oneko.docker.ReadableDockerRegistry;
import io.oneko.docker.WritableDockerRegistry;
import io.oneko.docker.event.EventAwareDockerRegistryRepository;
import io.oneko.event.EventDispatcher;

@Service
@Profile(Profiles.IN_MEMORY)
public class DockerRegistryInMemoryRepository extends EventAwareDockerRegistryRepository {

	private final Map<UUID, ReadableDockerRegistry> innerRepository = new HashMap<>();

	public DockerRegistryInMemoryRepository(EventDispatcher eventDispatcher) {
		super(eventDispatcher);
	}

	@Override
	protected ReadableDockerRegistry addInternally(WritableDockerRegistry dockerRegistry) {
		final ReadableDockerRegistry readable = dockerRegistry.readable();
		innerRepository.put(dockerRegistry.getId(), readable);
		return readable;
	}

	@Override
	protected void removeInternally(DockerRegistry dockerRegistry) {
		innerRepository.remove(dockerRegistry.getUuid());
	}

	@Override
	public Optional<ReadableDockerRegistry> getById(UUID registryId) {
		return Optional.ofNullable(innerRepository.get(registryId));
	}

	@Override
	public Optional<ReadableDockerRegistry> getByName(String registryName) {
		return innerRepository.values().stream()
				.filter(registry -> registry.getName().equals(registryName))
				.findFirst();
	}

	@Override
	public List<ReadableDockerRegistry> getAll() {
		return new ArrayList<>(innerRepository.values());
	}
}
