package io.oneko.helm;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HelmRegistryRepository {
  // name url username password
	Optional<ReadableHelmRegistry> getById(UUID registryId);

	Optional<ReadableHelmRegistry> getByName(String registryName);

	List<ReadableHelmRegistry> getAll();

	ReadableHelmRegistry add(WritableHelmRegistry registry);

	void remove(HelmRegistry registry);
}
