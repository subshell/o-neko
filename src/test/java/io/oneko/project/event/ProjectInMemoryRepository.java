package io.oneko.project.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Profile(Profiles.IN_MEMORY)
public class ProjectInMemoryRepository extends EventAwareProjectRepository {

	private final Map<UUID, ReadableProject> projects = new HashMap<>();

	protected ProjectInMemoryRepository(EventDispatcher eventDispatcher) {
		super(eventDispatcher);
	}

	@Override
	protected Mono<ReadableProject> addInternally(WritableProject project) {
		final ReadableProject readable = project.readable();
		this.projects.put(project.getId(), readable);
		return Mono.just(readable);
	}

	@Override
	protected Mono<Void> removeInternally(Project<?, ?> project) {
		this.projects.remove(project.getId());
		return Mono.empty();
	}

	@Override
	public Mono<ReadableProject> getById(UUID projectId) {
		return Mono.just(this.projects.get(projectId));
	}

	@Override
	public Mono<ReadableProject> getByName(String name) {
		return this.projects.values()
				.stream()
				.filter(p -> StringUtils.equals(name, p.getName()))
				.findFirst()
				.map(Mono::just)
				.orElse(Mono.empty());
	}

	@Override
	public Flux<ReadableProject> getByDockerRegistryUuid(UUID dockerRegistryUUID) {
		List<ReadableProject> collect = this.projects.values()
				.stream()
				.filter(p -> Objects.equals(dockerRegistryUUID, p.getDockerRegistryId()))
				.collect(Collectors.toList());
		return Flux.fromIterable(collect);
	}

	@Override
	public Flux<ReadableProject> getAll() {
		Collection<ReadableProject> values = new ArrayList<>(this.projects.values());
		return Flux.fromIterable(values);
	}
}
