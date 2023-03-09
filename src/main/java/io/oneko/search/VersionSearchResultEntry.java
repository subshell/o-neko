package io.oneko.search;

import io.oneko.project.ReadableProjectVersion;
import java.util.UUID;

public record VersionSearchResultEntry(String name, UUID id, String projectName, UUID projectId) {

	public static VersionSearchResultEntry of(ReadableProjectVersion version) {
		var project = version.getProject();
		return new VersionSearchResultEntry(version.getName(), version.getId(), project.getName(), project.getId());
	}
}
