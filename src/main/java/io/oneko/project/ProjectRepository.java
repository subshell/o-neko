package io.oneko.project;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Stores projects.
 * That's pretty much it.
 */
public interface ProjectRepository {

	Mono<Project> getById(UUID projectId);

	Mono<Project> getByName(String name);

	Flux<Project> getByDockerRegistryUuid(UUID dockerRegistryUUID);

	Flux<Project> getAll();

	/**
	 * Persists the project.
	 */
	Mono<Project> add(Project project);

	Mono<Void> remove(Project project);
}
