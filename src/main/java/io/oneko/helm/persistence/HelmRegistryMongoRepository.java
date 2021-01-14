package io.oneko.helm.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.helm.HelmRegistry;
import io.oneko.helm.ReadableHelmRegistry;
import io.oneko.helm.WritableHelmRegistry;
import io.oneko.helm.event.EventAwareHelmRegistryRepository;
import io.oneko.security.AES;

@Service
@Profile(Profiles.MONGO)
public class HelmRegistryMongoRepository extends EventAwareHelmRegistryRepository {
	private final HelmRegistryMongoSpringRepository innerRepo;
	private final AES credentialsCoder;

	@Autowired
	HelmRegistryMongoRepository(HelmRegistryMongoSpringRepository innerRepo,
															AES credentialsCoder,
															EventDispatcher eventDispatcher) {
		super(eventDispatcher);
		this.innerRepo = innerRepo;
		this.credentialsCoder = credentialsCoder;
	}

	@Override
	public Optional<ReadableHelmRegistry> getById(UUID registryId) {
		return this.innerRepo.findById(registryId).map(this::fromRegistryMongo);
	}

	@Override
	public Optional<ReadableHelmRegistry> getByName(String name) {
		return this.innerRepo.findByName(name).map(this::fromRegistryMongo);
	}

	@Override
	public List<ReadableHelmRegistry> getAll() {
		return this.innerRepo.findAll().stream()
				.map(this::fromRegistryMongo)
				.collect(Collectors.toList());
	}

	@Override
	protected ReadableHelmRegistry addInternally(WritableHelmRegistry registry) {
		HelmRegistryMongo registryMongo = toRegistryMongo(registry);
		return fromRegistryMongo(this.innerRepo.save(registryMongo));
	}

	@Override
	protected void removeInternally(HelmRegistry registry) {
		this.innerRepo.deleteById(registry.getId());
	}

	    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Mapping stuff
     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

	private HelmRegistryMongo toRegistryMongo(WritableHelmRegistry registry) {
		return HelmRegistryMongo.builder()
				.id(registry.getId())
				.name(registry.getName())
				.url(registry.getUrl())
				.username(registry.getUsername())
				.password(credentialsCoder.encrypt(registry.getId().toString() + registry.getPassword()))
				.build();
	}

	private ReadableHelmRegistry fromRegistryMongo(HelmRegistryMongo registryMongo) {
		return ReadableHelmRegistry.builder()
				.id(registryMongo.getId())
				.name(registryMongo.getName())
				.url(registryMongo.getUrl())
				.username(registryMongo.getUsername())
				.password(StringUtils.substringAfter(credentialsCoder.decrypt(registryMongo.getPassword()), registryMongo.getId().toString()))
				.build();
	}
}
