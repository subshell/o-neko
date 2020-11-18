package io.oneko.projectmesh.persistence;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.projectmesh.ProjectMesh;
import io.oneko.projectmesh.ReadableProjectMesh;
import io.oneko.projectmesh.WritableProjectMesh;
import io.oneko.projectmesh.event.EventAwareProjectMeshRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Profile(Profiles.IN_MEMORY)
public class ProjectMeshInMemoryRepository extends EventAwareProjectMeshRepository {

	private final Map<UUID, ReadableProjectMesh> meshes = new HashMap<>();

	public ProjectMeshInMemoryRepository(EventDispatcher eventDispatcher) {
		super(eventDispatcher);
	}

	@Override
	protected ReadableProjectMesh addInternally(WritableProjectMesh mesh) {
		final ReadableProjectMesh readable = mesh.readable();
		this.meshes.put(readable.getId(), readable);
		return readable;
	}

	@Override
	protected void removeInternally(ProjectMesh<?, ?> mesh) {
		this.meshes.remove(mesh.getId());
	}

	@Override
	public Optional<ReadableProjectMesh> getById(UUID id) {
		return Optional.ofNullable(this.meshes.get(id));
	}

	@Override
	public Optional<ReadableProjectMesh> getByName(String name) {
		return this.meshes.values()
				.stream()
				.filter(p -> StringUtils.equals(name, p.getName()))
				.findFirst();
	}

	@Override
	public List<ReadableProjectMesh> getAll() {
		return new ArrayList<>(this.meshes.values());
	}
}
