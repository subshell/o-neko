package io.oneko.namespace.persistence;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.oneko.event.EventDispatcher;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.event.EventAwareDefinedNamespaceRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DefinedNamespaceMongoRepository extends EventAwareDefinedNamespaceRepository {

	private final DefinedNamespaceMongoSpringRepository innerRepo;

	@Autowired
	public DefinedNamespaceMongoRepository(DefinedNamespaceMongoSpringRepository innerRepo, EventDispatcher eventDispatcher) {
		super(eventDispatcher);
		this.innerRepo = innerRepo;
	}

	@Override
	protected Mono<DefinedNamespace> addInternally(DefinedNamespace namespace) {
		return this.innerRepo.save(this.toNamespaceMongo(namespace)).map(this::fromNamespaceMongo);
	}

	@Override
	protected Mono<Void> removeInternally(DefinedNamespace namespace) {
		return innerRepo.deleteById(namespace.getId());
	}

	@Override
	public Mono<DefinedNamespace> getById(UUID id) {
		return this.innerRepo.findById(id).map(this::fromNamespaceMongo);
	}

	@Override
	public Mono<DefinedNamespace> getByName(String name) {
		return this.innerRepo.findByName(name).map(this::fromNamespaceMongo);
	}

	@Override
	public Flux<DefinedNamespace> getAll() {
		return this.innerRepo.findAll().map(this::fromNamespaceMongo);
	}

	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Mapping stuff
     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

	private DefinedNamespaceMongo toNamespaceMongo(DefinedNamespace namespace) {
		DefinedNamespaceMongo namespaceMongo = new DefinedNamespaceMongo();
		namespaceMongo.setId(namespace.getId());
		namespaceMongo.setName(namespace.asKubernetesNameSpace());
		return namespaceMongo;
	}

	private DefinedNamespace fromNamespaceMongo(DefinedNamespaceMongo namespaceMongo) {
		return DefinedNamespace.builder()
				.id(namespaceMongo.getId())
				.name(namespaceMongo.getName())
				.build();
	}
}
