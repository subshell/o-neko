package io.oneko.helm.api;

import java.io.File;
import java.util.List;

import io.oneko.helm.model.Chart;
import io.oneko.helm.model.InstallStatus;
import io.oneko.helm.model.Release;
import io.oneko.helm.model.Repository;
import io.oneko.helm.model.Status;
import io.oneko.helm.model.Values;

public interface Helm3API {

	// install
	default InstallStatus install(String name, String chart, Values values) {
		return install(name, chart, values, null);
	}

	default InstallStatus install(String name, String chart, Values values, String namespace) {
		return install(name, chart, values, namespace, false);
	}

	default InstallStatus install(String name, File pathToChartDirectory, Values values, String namespace, boolean dryRun) {
		return install(name, pathToChartDirectory.getAbsolutePath(), values, namespace, dryRun);
	}

	InstallStatus install(String name, String chart, Values values, String namespace, boolean dryRun);

	// list
	default List<Release> list() {
		return list(null, null);
	}

	List<Release> list(String namespace, String filter);

	default List<Release> listInAllNamespaces() {
		return listInAllNamespaces(null);
	}

	List<Release> listInAllNamespaces(String filter);

	// repo
	void addRepo(String name, String url, String username, String password);

	List<Repository> listRepos();

	void removeRepo(String name);

	void updateRepos();

	// search
	enum SearchLocation { hub, repo }
	default List<Chart> searchHub(String query) {
		return search(SearchLocation.hub, query);
	}
	default List<Chart> searchRepo(String query) {
		return search(SearchLocation.repo, query);
	}
	List<Chart> search(SearchLocation location, String query);

	// status
	default Status status(String releaseName) {
		return status(releaseName, null);
	}

	Status status(String releaseName, String namespace);

	// uninstall
	default void uninstall(String name) {
		uninstall(name, null);
	}
	default void uninstall(String name, String namespace) {
		uninstall(name, namespace, false);
	}
	void uninstall(String name, String namespace, boolean dryRun);

	// version
	String version();
}
