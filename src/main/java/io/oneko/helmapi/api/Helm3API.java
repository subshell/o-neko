package io.oneko.helmapi.api;

import java.io.File;
import java.util.List;

import io.oneko.helmapi.model.Chart;
import io.oneko.helmapi.model.InstallStatus;
import io.oneko.helmapi.model.Release;
import io.oneko.helmapi.model.Repository;
import io.oneko.helmapi.model.Status;
import io.oneko.helmapi.model.Values;

public interface Helm3API {

	// install
	default InstallStatus install(String name, String chart, Values values) {
		return install(name, chart, values, null);
	}

	default InstallStatus install(String name, String chart, Values values, String namespace) {
		return install(name, chart, null, values, namespace, false);
	}

	default InstallStatus install(String name, File pathToChartDirectory, Values values, String namespace, boolean dryRun) {
		return install(name, pathToChartDirectory.getAbsolutePath(), null, values, namespace, dryRun);
	}

	InstallStatus install(String name, String chart, String version, Values values, String namespace, boolean dryRun);

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
	default void addRepo(String name, String url, String username, String password) {
		addRepo(name, url, username, password, false, true);
	}

	void addRepo(String name, String url, String username, String password, boolean forceUpdate, boolean passCredentials);

	List<Repository> listRepos();

	void removeRepo(String name);

	void updateRepos();

	// search
	List<Chart> searchHub(String query);

	List<Chart> searchRepo(String query, boolean versions, boolean devel);

	default List<Chart> searchRepo(String query) {
		return searchRepo(query, false, false);
	}

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
