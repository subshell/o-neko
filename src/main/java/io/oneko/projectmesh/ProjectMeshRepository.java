package io.oneko.projectmesh;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProjectMeshRepository {

	Mono<ProjectMesh> getById(UUID id);

	Mono<ProjectMesh> getByName(String name);

	Flux<ProjectMesh> getAll();

	Mono<ProjectMesh> add(ProjectMesh mesh);

	Mono<Void> remove(ProjectMesh mesh);
}
