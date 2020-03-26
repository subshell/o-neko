package io.oneko.projectmesh;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProjectMeshRepository {

	Mono<ReadableProjectMesh> getById(UUID id);

	Mono<ReadableProjectMesh> getByName(String name);

	Flux<ReadableProjectMesh> getAll();

	Mono<ReadableProjectMesh> add(WritableProjectMesh mesh);

	Mono<Void> remove(ProjectMesh<?, ?> mesh);
}
