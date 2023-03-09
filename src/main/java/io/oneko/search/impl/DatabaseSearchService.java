package io.oneko.search.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import io.oneko.event.EventDispatcher;
import io.oneko.project.ProjectRepository;
import io.oneko.project.event.ProjectDeletedEvent;
import io.oneko.project.event.ProjectSavedEvent;
import io.oneko.search.MeasuringSearchService;
import io.oneko.search.ProjectSearchResultEntry;
import io.oneko.search.SearchResult;
import io.oneko.search.VersionSearchResultEntry;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class DatabaseSearchService extends MeasuringSearchService {

	private final ProjectRepository projectRepository;
	private final Cache<String, SearchResult> queryResultCache = Caffeine.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS)
			.maximumSize(512)
			.build();

	public DatabaseSearchService(ProjectRepository projectRepository,
			EventDispatcher eventDispatcher, MeterRegistry meterRegistry) {
		super(meterRegistry);

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
	public SearchResult findProjectsAndVersionsInternal(String searchTerm) {
		return queryResultCache.get(searchTerm, s -> {
			var versions = projectRepository.findProjectVersion(searchTerm)
					.stream()
					.map(VersionSearchResultEntry::of)
					.toList();
			var projects = projectRepository.findProject(searchTerm)
					.stream()
					.map(ProjectSearchResultEntry::of)
					.toList();

			return SearchResult.builder()
					.query(searchTerm)
					.projects(projects)
					.versions(versions)
					.build();
		});
	}
}
