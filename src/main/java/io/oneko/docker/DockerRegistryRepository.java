package io.oneko.docker;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DockerRegistryRepository {

	Mono<ReadableDockerRegistry> getById(UUID registryId);

	Mono<ReadableDockerRegistry> getByName(String registryName);

	Flux<ReadableDockerRegistry> getAll();

	Mono<ReadableDockerRegistry> add(WritableDockerRegistry registry);

	Mono<Void> remove(DockerRegistry registry);
}
