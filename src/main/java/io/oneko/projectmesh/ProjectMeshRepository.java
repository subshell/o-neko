package io.oneko.projectmesh;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMeshRepository {

	Optional<ReadableProjectMesh> getById(UUID id);

	Optional<ReadableProjectMesh> getByName(String name);

	List<ReadableProjectMesh> getAll();

	ReadableProjectMesh add(WritableProjectMesh mesh);

	void remove(ProjectMesh<?, ?> mesh);
}
