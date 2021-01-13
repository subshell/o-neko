package io.oneko.docker;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DockerRegistryRepository {

	Optional<ReadableDockerRegistry> getById(UUID registryId);

	Optional<ReadableDockerRegistry> getByName(String registryName);

	List<ReadableDockerRegistry> getAll();

	ReadableDockerRegistry add(WritableDockerRegistry registry);

	void remove(DockerRegistry registry);
}
