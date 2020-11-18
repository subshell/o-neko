package io.oneko.project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores projects.
 * That's pretty much it.
 */
public interface ProjectRepository {

	Optional<ReadableProject> getById(UUID projectId);

	Optional<ReadableProject> getByName(String name);

	List<ReadableProject> getByDockerRegistryUuid(UUID dockerRegistryUUID);

	List<ReadableProject> getAll();

	/**
	 * Persists the project.
	 */
	ReadableProject add(WritableProject project);

	void remove(Project<?, ?> project);
}
