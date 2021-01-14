package io.oneko.helm.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.helm.HelmRegistry;
import io.oneko.helm.ReadableHelmRegistry;
import io.oneko.helm.WritableHelmRegistry;
import io.oneko.helm.event.EventAwareHelmRegistryRepository;

@Service
@Profile(Profiles.IN_MEMORY)
public class HelmRegistryInMemoryRepository extends EventAwareHelmRegistryRepository {

	private final Map<UUID, ReadableHelmRegistry> innerRepository = new ConcurrentHashMap<>();

	public HelmRegistryInMemoryRepository(EventDispatcher eventDispatcher) {
		super(eventDispatcher);
	}

	@Override
	public Optional<ReadableHelmRegistry> getById(UUID registryId) {
		return Optional.ofNullable(innerRepository.get(registryId));
	}

	@Override
	public Optional<ReadableHelmRegistry> getByName(String registryName) {
		return innerRepository.values().stream()
				.filter(registry -> registry.getName().equals(registryName))
				.findFirst();
	}

	@Override
	public List<ReadableHelmRegistry> getAll() {
		return List.copyOf(innerRepository.values());
	}

	@Override
	protected ReadableHelmRegistry addInternally(WritableHelmRegistry helmRegistry) {
		ReadableHelmRegistry readable = helmRegistry.readable();
		innerRepository.put(helmRegistry.getId(), readable);
		return readable;
	}

	@Override
	protected void removeInternally(HelmRegistry helmRegistry) {
		innerRepository.remove(helmRegistry.getId());
	}
}
