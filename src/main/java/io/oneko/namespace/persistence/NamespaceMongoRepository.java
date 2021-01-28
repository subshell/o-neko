package io.oneko.namespace.persistence;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.namespace.Namespace;
import io.oneko.namespace.ReadableNamespace;
import io.oneko.namespace.WritableNamespace;
import io.oneko.namespace.event.EventAwareNamespaceRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Profile(Profiles.MONGO)
public class NamespaceMongoRepository extends EventAwareNamespaceRepository {

	private final DefinedNamespaceMongoSpringRepository innerRepo;

	public NamespaceMongoRepository(DefinedNamespaceMongoSpringRepository innerRepo, EventDispatcher eventDispatcher) {
		super(eventDispatcher);
		this.innerRepo = innerRepo;
	}

	@Override
	protected ReadableNamespace addInternally(WritableNamespace namespace) {
		return this.fromNamespaceMongo(this.innerRepo.save(this.toNamespaceMongo(namespace)));
	}

	@Override
	protected void removeInternally(Namespace namespace) {
		innerRepo.deleteById(namespace.getId());
	}

	@Override
	public Optional<ReadableNamespace> getById(UUID id) {
		return this.innerRepo.findById(id).map(this::fromNamespaceMongo);
	}

	@Override
	public Optional<ReadableNamespace> getByName(String name) {
		return this.innerRepo.findByName(name).map(this::fromNamespaceMongo);
	}

	@Override
	public List<ReadableNamespace> getAll() {
		return this.innerRepo.findAll().stream().map(this::fromNamespaceMongo).collect(Collectors.toList());
	}

	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Mapping stuff
     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

	private NamespaceMongo toNamespaceMongo(Namespace namespace) {
		NamespaceMongo namespaceMongo = new NamespaceMongo();
		namespaceMongo.setId(namespace.getId());
		namespaceMongo.setName(namespace.asKubernetesNameSpace());
		return namespaceMongo;
	}

	private ReadableNamespace fromNamespaceMongo(NamespaceMongo namespaceMongo) {
		return ReadableNamespace.builder()
				.id(namespaceMongo.getId())
				.name(namespaceMongo.getName())
				.build();
	}
}
