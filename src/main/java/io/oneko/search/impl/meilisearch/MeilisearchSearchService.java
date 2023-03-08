package io.oneko.search.impl.meilisearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Config;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.exceptions.MeilisearchException;
import com.meilisearch.sdk.json.GsonJsonHandler;
import com.meilisearch.sdk.model.Searchable;
import io.micrometer.core.instrument.MeterRegistry;
import io.oneko.event.EventDispatcher;
import io.oneko.project.ProjectRepository;
import io.oneko.project.ReadableProject;
import io.oneko.project.ReadableProjectVersion;
import io.oneko.project.event.ProjectDeletedEvent;
import io.oneko.project.event.ProjectSavedEvent;
import io.oneko.search.MeasuringSearchService;
import io.oneko.search.ProjectSearchResultEntry;
import io.oneko.search.SearchResult;
import io.oneko.search.VersionSearchResultEntry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "o-neko.search.meilisearch.enabled", havingValue = "true")
public class MeilisearchSearchService extends MeasuringSearchService {

	private final ObjectMapper objectMapper;
	private final Index projectIndex;
	private final Index versionIndex;
	private final Gson gson = new Gson();
	private final Cache<String, SearchResult> queryResultCache = Caffeine.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS)
			.maximumSize(512)
			.build(); // TODO: Evaluate if cache is really useful in combination with Meilisearch

	public MeilisearchSearchService(ProjectRepository projectRepository,
			EventDispatcher eventDispatcher,
			ObjectMapper objectMapper,
			MeterRegistry meterRegistry) throws MeilisearchException {
		super(meterRegistry);

		this.objectMapper = objectMapper;

		Client client = new Client(
				new Config("http://localhost:7700", "ZWEZtwJKXZX3mQGA9hlqKr7THr2UTcHOKThuV8aWq3A", new GsonJsonHandler())); // TODO: Read values from config
		projectIndex = client.index("oneko_projects");
		versionIndex = client.index("oneko_versions");

		boolean initIndexes = true; // TODO: Check if index exists or is empty

		eventDispatcher.registerListener(event -> {
			if (event instanceof ProjectSavedEvent pse) {
				UUID projectId = pse.describeEntityChange()
						.getId();
				projectRepository.getById(projectId)
						.ifPresent(project -> {
							delete(project.getId()
									.toString());
							indexProject(project);
						});
				queryResultCache.invalidateAll();
			} else if (event instanceof ProjectDeletedEvent pde) {
				String deletedProjectId = pde.describeEntityChange()
						.getId()
						.toString();
				delete(deletedProjectId);
				queryResultCache.invalidateAll();
			}
		});

		if (initIndexes) {
			CompletableFuture.runAsync(() -> projectRepository.getAll()
					.forEach(this::indexProject));
		}
	}

	private void indexProject(ReadableProject project) {
		ProjectMeili projectMeili = toProjectMeili(project);
		try {
			String json = gson.toJson(projectMeili);
			projectIndex.addDocuments(json, "id");
		} catch (MeilisearchException e) {
			throw new RuntimeException(e);
		}
		project.getVersions()
				.forEach(this::indexVersion);
	}

	private void indexVersion(ReadableProjectVersion version) {
		VersionMeili versionMeili = toVersionMeili(version);
		try {
			String json = gson.toJson(versionMeili);
			versionIndex.addDocuments(json, "id");
		} catch (MeilisearchException e) {
			throw new RuntimeException(e);
		}
	}

	private ProjectMeili toProjectMeili(ReadableProject project) {
		return ProjectMeili.builder()
				.id(project.getId())
				.name(project.getName())
				.containerImage(project.getImageName())
				.versionNames(project.getVersions()
						.stream()
						.map(ReadableProjectVersion::getName)
						.toList())
				.build();
	}

	private VersionMeili toVersionMeili(ReadableProjectVersion version) {
		ReadableProject project = version.getProject();
		return VersionMeili.builder()
				.id(version.getId())
				.name(version.getName())
				.projectId(project.getId())
				.projectName(project.getName())
				.build();
	}

	private void delete(String projectId) {
		try {
			projectIndex.deleteDocument(projectId);
			Searchable search = versionIndex.search(new SearchRequest("").setFilter(new String[]{"projectId = " + projectId}));
			search.getHits()
					.stream()
					.map(h -> h.get("id"))
					.forEach(id -> {
						if (id instanceof String) {
							try {
								versionIndex.deleteDocument((String) id);
							} catch (MeilisearchException e) {
								throw new RuntimeException(e);
							}
						}
					});
		} catch (MeilisearchException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public SearchResult findProjectsAndVersions(String searchTerm) {
		return queryResultCache.get(searchTerm, s -> {
			try {
				var versions = versionIndex.search(searchTerm)
						.getHits()
						.stream()
						.map(res -> objectMapper.convertValue(res, VersionMeili.class))
						.map(vm -> new VersionSearchResultEntry(vm.getName(), vm.getId(), vm.getProjectName(), vm.getProjectId()))
						.toList();

				var projects = projectIndex.search(searchTerm)
						.getHits()
						.stream()
						.map(res -> objectMapper.convertValue(res, ProjectMeili.class))
						.map(pm -> new ProjectSearchResultEntry(pm.getName(), pm.getId()))
						.toList();

				return SearchResult.builder()
						.query(searchTerm)
						.projects(projects)
						.versions(versions)
						.build();
			} catch (MeilisearchException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
