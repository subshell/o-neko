package io.oneko.search;

import io.oneko.project.ReadableProject;
import java.util.UUID;

public record ProjectSearchResultEntry(String name, UUID id) {

	public static ProjectSearchResultEntry of(ReadableProject project) {
		return new ProjectSearchResultEntry(project.getName(), project.getId());
	}
}
