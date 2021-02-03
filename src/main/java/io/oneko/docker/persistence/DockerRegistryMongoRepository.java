package io.oneko.docker.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.docker.DockerRegistry;
import io.oneko.docker.ReadableDockerRegistry;
import io.oneko.docker.WritableDockerRegistry;
import io.oneko.docker.event.EventAwareDockerRegistryRepository;
import io.oneko.event.EventDispatcher;
import io.oneko.security.AES;

@Service
@Profile(Profiles.MONGO)
class DockerRegistryMongoRepository extends EventAwareDockerRegistryRepository {
	private final DockerRegistryMongoSpringRepository innerRepo;
	private final AES credentialsCoder;

	@Autowired
	DockerRegistryMongoRepository(DockerRegistryMongoSpringRepository innerRepo, AES credentialsCoder, EventDispatcher eventDispatcher) {
		super(eventDispatcher);
		this.innerRepo = innerRepo;
		this.credentialsCoder = credentialsCoder;
	}

	@Override
	public Optional<ReadableDockerRegistry> getById(UUID registryId) {
		return this.innerRepo.findById(registryId).map(this::fromRegistryMongo);
	}

	@Override
	public Optional<ReadableDockerRegistry> getByName(String name) {
		return this.innerRepo.findByName(name).map(this::fromRegistryMongo);
	}

	@Override
	public List<ReadableDockerRegistry> getAll() {
		return this.innerRepo.findAll().stream().map(this::fromRegistryMongo).collect(Collectors.toList());
	}

	@Override
	protected ReadableDockerRegistry addInternally(WritableDockerRegistry registry) {
		DockerRegistryMongo registryMongo = this.toRegistryMongo(registry);
		return this.fromRegistryMongo(this.innerRepo.save(registryMongo));
	}

	@Override
	protected void removeInternally(DockerRegistry registry) {
		this.innerRepo.deleteById(registry.getUuid());
	}

    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * Mapping stuff
     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

	private DockerRegistryMongo toRegistryMongo(WritableDockerRegistry registry) {
		DockerRegistryMongo registryMongo = new DockerRegistryMongo();
		registryMongo.setRegistryUuid(registry.getUuid());
		registryMongo.setName(registry.getName());
		registryMongo.setRegistryUrl(registry.getRegistryUrl());
		registryMongo.setUserName(registry.getUserName());
		registryMongo.setPassword(credentialsCoder.encrypt(registry.getUuid().toString() + registry.getPassword()));
		registryMongo.setTrustInsecureCertificate(registry.isTrustInsecureCertificate());
		return registryMongo;
	}

	private ReadableDockerRegistry fromRegistryMongo(DockerRegistryMongo registryMongo) {
		return ReadableDockerRegistry.builder()
				.uuid(registryMongo.getRegistryUuid())
				.name(registryMongo.getName())
				.registryUrl(registryMongo.getRegistryUrl())
				.userName(registryMongo.getUserName())
				.password(StringUtils.substringAfter(credentialsCoder.decrypt(registryMongo.getPassword()), registryMongo.getRegistryUuid().toString()))
				.trustInsecureCertificate(registryMongo.isTrustInsecureCertificate())
				.build();
	}
}
