package io.oneko.project.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.event.EventDispatcher;
import io.oneko.project.Project;
import io.oneko.project.ReadableProject;
import io.oneko.project.WritableProject;

@Service
@Profile(Profiles.IN_MEMORY)
public class ProjectInMemoryRepository extends EventAwareProjectRepository {

	private final Map<UUID, ReadableProject> projects = new HashMap<>();

	protected ProjectInMemoryRepository(EventDispatcher eventDispatcher) {
		super(eventDispatcher);
	}

	@Override
	protected ReadableProject addInternally(WritableProject project) {
		final ReadableProject readable = project.readable();
		this.projects.put(project.getId(), readable);
		return readable;
	}

	@Override
	protected void removeInternally(Project<?, ?> project) {
		this.projects.remove(project.getId());
	}

	@Override
	public Optional<ReadableProject> getById(UUID projectId) {
		return Optional.ofNullable(this.projects.get(projectId));
	}

	@Override
	public Optional<ReadableProject> getByName(String name) {
		return this.projects.values()
				.stream()
				.filter(p -> StringUtils.equals(name, p.getName()))
				.findFirst();
	}

	@Override
	public List<ReadableProject> getByDockerRegistryUuid(UUID dockerRegistryUUID) {
		return this.projects.values()
				.stream()
				.filter(p -> Objects.equals(dockerRegistryUUID, p.getDockerRegistryId()))
				.collect(Collectors.toList());
	}

	@Override
	public List<ReadableProject> getAll() {
		return new ArrayList<>(this.projects.values());
	}
}
