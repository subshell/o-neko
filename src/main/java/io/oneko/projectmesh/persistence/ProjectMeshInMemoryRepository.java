package io.oneko.projectmesh.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.projectmesh.ProjectMesh;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableProjectMesh;
import io.oneko.projectmesh.event.EventAwareProjectMeshRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Profile(Profiles.IN_MEMORY)
public class ProjectMeshInMemoryRepository extends EventAwareProjectMeshRepository {

	private Map<UUID, ReadableProjectMesh> meshes = new HashMap<>();

	protected ProjectMeshInMemoryRepository(EventDispatcher eventDispatcher) {
		super(eventDispatcher);
	}

	@Override
	protected Mono<ReadableProjectMesh> addInternally(WritableProjectMesh mesh) {
		final ReadableProjectMesh readable = mesh.readable();
		this.meshes.put(readable.getId(), readable);
		return Mono.just(readable);
	}

	@Override
	protected Mono<Void> removeInternally(ProjectMesh<?, ?> mesh) {
		this.meshes.remove(mesh.getId());
		return Mono.empty();
	}

	@Override
	public Mono<ReadableProjectMesh> getById(UUID id) {
		return Mono.just(this.meshes.get(id));
	}

	@Override
	public Mono<ReadableProjectMesh> getByName(String name) {
		return this.meshes.values()
				.stream()
				.filter(p -> StringUtils.equals(name, p.getName()))
				.findFirst()
				.map(Mono::just)
				.orElse(Mono.empty());
	}

	@Override
	public Flux<ReadableProjectMesh> getAll() {
		Collection<ReadableProjectMesh> values = new ArrayList<>(this.meshes.values());
		return Flux.fromIterable(values);
	}
}
