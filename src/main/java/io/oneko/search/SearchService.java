package io.oneko.search;

import java.util.List;

public interface SearchService {
	List<SearchResultEntry> findProjectsAndVersions(String searchTerm);

}
