package io.oneko.docker.persistence;

import java.util.UUID;

import io.oneko.Profiles;
import io.oneko.docker.WritableDockerRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oneko.docker.DockerRegistry;
import io.oneko.docker.event.EventAwareDockerRegistryRepository;
import io.oneko.event.EventDispatcher;
import io.oneko.security.AES;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
	public Mono<DockerRegistry> getById(UUID registryId) {
		return this.innerRepo.findById(registryId).map(this::fromRegistryMongo);
	}

	@Override
	public Mono<DockerRegistry> getByName(String name) {
		return this.innerRepo.findByName(name).map(this::fromRegistryMongo);
	}

	@Override
	public Flux<DockerRegistry> getAll() {
		return this.innerRepo.findAll().map(this::fromRegistryMongo);
	}

	@Override
	protected Mono<DockerRegistry> addInternally(WritableDockerRegistry registry) {
		DockerRegistryMongo registryMongo = this.toRegistryMongo(registry);
		return this.innerRepo.save(registryMongo).map(this::fromRegistryMongo);
	}

	@Override
	protected Mono<Void> removeInternally(DockerRegistry registry) {
		return this.innerRepo.deleteById(registry.getId());
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

	private DockerRegistry fromRegistryMongo(DockerRegistryMongo registryMongo) {
		return DockerRegistry.builder()
				.uuid(registryMongo.getRegistryUuid())
				.name(registryMongo.getName())
				.registryUrl(registryMongo.getRegistryUrl())
				.userName(registryMongo.getUserName())
				.password(StringUtils.substringAfter(credentialsCoder.decrypt(registryMongo.getPassword()), registryMongo.getRegistryUuid().toString()))
				.trustInsecureCertificate(registryMongo.isTrustInsecureCertificate())
				.build();
	}
}
