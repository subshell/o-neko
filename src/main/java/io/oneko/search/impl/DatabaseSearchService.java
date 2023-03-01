package io.oneko.search.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Streams;
import io.oneko.event.EventDispatcher;
import io.oneko.project.ProjectRepository;
import io.oneko.project.event.ProjectDeletedEvent;
import io.oneko.project.event.ProjectSavedEvent;
import io.oneko.search.SearchResultEntry;
import io.oneko.search.SearchService;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class DatabaseSearchService implements SearchService {

	private final ProjectRepository projectRepository;
	private final Cache<String, List<SearchResultEntry>> queryResultCache = Caffeine.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS)
			.build();

	public DatabaseSearchService(ProjectRepository projectRepository, EventDispatcher eventDispatcher) {
		this.projectRepository = projectRepository;
		eventDispatcher.registerListener(event -> {
			if (event instanceof ProjectSavedEvent) {
				queryResultCache.invalidateAll();
			} else if (event instanceof ProjectDeletedEvent) {
				queryResultCache.invalidateAll();
			}
		});
	}

	@Override
	public List<SearchResultEntry> findProjectsAndVersions(String searchTerm) {
		return queryResultCache.get(searchTerm, s -> {
			var versions = projectRepository.findProjectVersion(searchTerm)
					.stream()
					.map(SearchResultEntry::of);
			var projects = projectRepository.findProject(searchTerm)
					.stream()
					.map(SearchResultEntry::of);
			return Streams.concat(versions, projects)
					.toList();
		});
	}
}
