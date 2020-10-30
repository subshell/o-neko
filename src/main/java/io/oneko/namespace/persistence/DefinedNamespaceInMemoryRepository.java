package io.oneko.namespace.persistence;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.ReadableDefinedNamespace;
import io.oneko.namespace.WritableDefinedNamespace;
import io.oneko.namespace.event.EventAwareDefinedNamespaceRepository;

@Service
@Profile(Profiles.IN_MEMORY)
public class DefinedNamespaceInMemoryRepository extends EventAwareDefinedNamespaceRepository {

	private final Map<UUID, ReadableDefinedNamespace> innerRepository = new HashMap<>();

	@Autowired
	DefinedNamespaceInMemoryRepository(EventDispatcher eventDispatcher) {
		super(eventDispatcher);
	}

	@Override
	protected ReadableDefinedNamespace addInternally(WritableDefinedNamespace namespace) {
		final ReadableDefinedNamespace readable = namespace.readable();
		this.innerRepository.put(readable.getId(), readable);
		return readable;
	}

	@Override
	protected void removeInternally(DefinedNamespace namespace) {
		innerRepository.remove(namespace.getId());
	}

	@Override
	public Optional<ReadableDefinedNamespace> getById(UUID id) {
		return Optional.ofNullable(innerRepository.get(id));
	}

	@Override
	public Optional<ReadableDefinedNamespace> getByName(String name) {
		return innerRepository.values().stream()
				.filter(namespace -> namespace.asKubernetesNameSpace().equals(name))
				.findFirst();
	}

	@Override
	public List<ReadableDefinedNamespace> getAll() {
		return new ArrayList<>(innerRepository.values());
	}
}
