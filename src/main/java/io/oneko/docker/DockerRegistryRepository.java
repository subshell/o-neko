package io.oneko.docker;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DockerRegistryRepository {

	Mono<DockerRegistry> getById(UUID registryId);

	Mono<DockerRegistry> getByName(String registryName);

	Flux<DockerRegistry> getAll();

	Mono<DockerRegistry> add(WritableDockerRegistry registry);

	Mono<Void> remove(DockerRegistry registry);
}
