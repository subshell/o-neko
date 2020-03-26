package io.oneko.project;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Stores projects.
 * That's pretty much it.
 */
public interface ProjectRepository {

	Mono<ReadableProject> getById(UUID projectId);

	Mono<ReadableProject> getByName(String name);

	Flux<ReadableProject> getByDockerRegistryUuid(UUID dockerRegistryUUID);

	Flux<ReadableProject> getAll();

	/**
	 * Persists the project.
	 */
	Mono<ReadableProject> add(WritableProject project);

	Mono<Void> remove(Project<?, ?> project);
}
