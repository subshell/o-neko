package io.oneko.namespace.persistence;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.namespace.Namespace;
import io.oneko.namespace.ReadableNamespace;
import io.oneko.namespace.WritableNamespace;
import io.oneko.namespace.event.EventAwareNamespaceRepository;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Profile(Profiles.IN_MEMORY)
public class NamespaceInMemoryRepository extends EventAwareNamespaceRepository {

	private final Map<UUID, ReadableNamespace> innerRepository = new HashMap<>();

	public NamespaceInMemoryRepository(EventDispatcher eventDispatcher) {
		super(eventDispatcher);
	}

	@Override
	protected ReadableNamespace addInternally(WritableNamespace namespace) {
		final ReadableNamespace readable = namespace.readable();
		this.innerRepository.put(readable.getId(), readable);
		return readable;
	}

	@Override
	protected void removeInternally(Namespace namespace) {
		innerRepository.remove(namespace.getId());
	}

	@Override
	public Optional<ReadableNamespace> getById(UUID id) {
		return Optional.ofNullable(innerRepository.get(id));
	}

	@Override
	public Optional<ReadableNamespace> getByName(String name) {
		return innerRepository.values().stream()
				.filter(namespace -> namespace.asKubernetesNameSpace().equals(name))
				.findFirst();
	}

	@Override
	public List<ReadableNamespace> getAll() {
		return new ArrayList<>(innerRepository.values());
	}
}
