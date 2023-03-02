package io.oneko.search;

import static io.oneko.search.SearchResultEntryType.PROJECT;
import static io.oneko.search.SearchResultEntryType.PROJECT_VERSION;

import io.oneko.project.ReadableProject;
import io.oneko.project.ReadableProjectVersion;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

public record SearchResultEntry(SearchResultEntryType type, String text, UUID id, UUID projectId) {
	public static SearchResultEntry of(ReadableProject project) {
		return new SearchResultEntry(PROJECT, project.getName(), project.getId(), project.getId());
	}

	public static SearchResultEntry of(ReadableProjectVersion projectVersion) {
		return new SearchResultEntry(PROJECT_VERSION, projectVersion.getName(), projectVersion.getId(), projectVersion.getProject().getId());
	}
}
