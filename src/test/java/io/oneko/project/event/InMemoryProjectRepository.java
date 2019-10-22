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

import io.oneko.event.EventDispatcher;
import io.oneko.project.Project;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * In memory dummy implementation.
 */
public class InMemoryProjectRepository extends EventAwareProjectRepository {

	private Map<UUID, Project> projects = new HashMap<>();

	protected InMemoryProjectRepository(EventDispatcher eventDispatcher) {
		super(eventDispatcher);
	}

	@Override
	protected Mono<Project> addInternally(Project project) {
		//TODO... remove dirty state
		this.projects.put(project.getId(), project);
		return Mono.just(project);
	}

	@Override
	protected Mono<Void> removeInternally(Project project) {
		this.projects.remove(project.getId());
		return Mono.empty();
	}

	@Override
	public Mono<Project> getById(UUID projectId) {
		return Mono.just(this.projects.get(projectId));
	}

	@Override
	public Mono<Project> getByName(String name) {
		return this.projects.values()
				.stream()
				.filter(p -> StringUtils.equals(name, p.getName()))
				.findFirst()
				.map(Mono::just)
				.orElse(Mono.empty());
	}

	@Override
	public Flux<Project> getByDockerRegistryUuid(UUID dockerRegistryUUID) {
		List<Project> collect = this.projects.values()
				.stream()
				.filter(p -> Objects.equals(dockerRegistryUUID, p.getDockerRegistryUuid()))
				.collect(Collectors.toList());
		return Flux.fromIterable(collect);
	}

	@Override
	public Flux<Project> getAll() {
		Collection<Project> values = new ArrayList<>(this.projects.values());
		return Flux.fromIterable(values);
	}
}
