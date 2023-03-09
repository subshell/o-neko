package io.oneko.search;

import java.util.Collection;
import java.util.List;
import lombok.Builder;

@Builder

public record SearchResult(String query, Collection<ProjectSearchResultEntry> projects, Collection<VersionSearchResultEntry> versions) {

	public SearchResult {
		projects = List.copyOf(projects);
		versions = List.copyOf(versions);
	}

	public int getTotal() {
		return getTotalProjectsFound() + getTotalVersionsFound();
	}

	public int getTotalProjectsFound() {
		if (projects != null) {
			return projects.size();
		}
		return 0;
	}

	public int getTotalVersionsFound() {
		if (versions != null) {
			return versions.size();
		}
		return 0;
	}
}
