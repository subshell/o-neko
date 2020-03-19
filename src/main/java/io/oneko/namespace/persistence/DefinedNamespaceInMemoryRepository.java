package io.oneko.namespace.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.ReadableDefinedNamespace;
import io.oneko.namespace.WritableDefinedNamespace;
import io.oneko.namespace.event.EventAwareDefinedNamespaceRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Profile(Profiles.IN_MEMORY)
public class DefinedNamespaceInMemoryRepository extends EventAwareDefinedNamespaceRepository {

	private final Map<UUID, ReadableDefinedNamespace> innerRepository = new HashMap<>();

	@Autowired
	DefinedNamespaceInMemoryRepository(EventDispatcher eventDispatcher) {
		super(eventDispatcher);
	}

	@Override
	protected Mono<ReadableDefinedNamespace> addInternally(WritableDefinedNamespace namespace) {
		final ReadableDefinedNamespace readable = namespace.readable();
		this.innerRepository.put(readable.getId(), readable);
		return Mono.just(readable);
	}

	@Override
	protected Mono<Void> removeInternally(DefinedNamespace namespace) {
		innerRepository.remove(namespace.getId());
		return Mono.empty();
	}

	@Override
	public Mono<ReadableDefinedNamespace> getById(UUID id) {
		return Mono.justOrEmpty(innerRepository.get(id));
	}

	@Override
	public Mono<ReadableDefinedNamespace> getByName(String name) {
		return Mono.justOrEmpty(innerRepository.values().stream()
				.filter(namespace -> namespace.asKubernetesNameSpace().equals(name))
				.findFirst());
	}

	@Override
	public Flux<ReadableDefinedNamespace> getAll() {
		return Flux.fromIterable(innerRepository.values());
	}
}
