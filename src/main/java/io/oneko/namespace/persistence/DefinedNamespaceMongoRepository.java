package io.oneko.namespace.persistence;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.namespace.DefinedNamespace;
import io.oneko.namespace.ReadableDefinedNamespace;
import io.oneko.namespace.WritableDefinedNamespace;
import io.oneko.namespace.event.EventAwareDefinedNamespaceRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Profile(Profiles.MONGO)
public class DefinedNamespaceMongoRepository extends EventAwareDefinedNamespaceRepository {

	private final DefinedNamespaceMongoSpringRepository innerRepo;

	public DefinedNamespaceMongoRepository(DefinedNamespaceMongoSpringRepository innerRepo, EventDispatcher eventDispatcher) {
		super(eventDispatcher);
		this.innerRepo = innerRepo;
	}

	@Override
	protected ReadableDefinedNamespace addInternally(WritableDefinedNamespace namespace) {
		return this.fromNamespaceMongo(this.innerRepo.save(this.toNamespaceMongo(namespace)));
	}

	@Override
	protected void removeInternally(DefinedNamespace namespace) {
		innerRepo.deleteById(namespace.getId());
	}

	@Override
	public Optional<ReadableDefinedNamespace> getById(UUID id) {
		return this.innerRepo.findById(id).map(this::fromNamespaceMongo);
	}

	@Override
	public Optional<ReadableDefinedNamespace> getByName(String name) {
		return this.innerRepo.findByName(name).map(this::fromNamespaceMongo);
	}

	@Override
	public List<ReadableDefinedNamespace> getAll() {
		return this.innerRepo.findAll().stream().map(this::fromNamespaceMongo).collect(Collectors.toList());
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

	private ReadableDefinedNamespace fromNamespaceMongo(DefinedNamespaceMongo namespaceMongo) {
		return ReadableDefinedNamespace.builder()
				.id(namespaceMongo.getId())
				.name(namespaceMongo.getName())
				.build();
	}
}
