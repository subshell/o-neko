package io.oneko.helmapi.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.reflect.TypeToken;

import io.oneko.helmapi.model.Chart;
import io.oneko.helmapi.model.InstallStatus;
import io.oneko.helmapi.model.Release;
import io.oneko.helmapi.model.Repository;
import io.oneko.helmapi.model.Status;
import io.oneko.helmapi.model.Values;
import io.oneko.helmapi.process.CommandExecutor;
import io.oneko.helmapi.process.DelegatingCommandExecutor;
import io.oneko.helmapi.process.ICommandExecutor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Helm implements Helm3API {

	private DelegatingCommandExecutor executor;
	private final Map<String, String> defaultGlobalFlags;

	public Helm() {
		this(new CommandExecutor());
	}

	// visibleForTesting
	Helm(ICommandExecutor executor) {
		this.executor = new DelegatingCommandExecutor(executor);
		this.defaultGlobalFlags = Map.of();
	}

	public Helm(String defaultNamespace) {
		Map<String, String> defaultGlobalFlags = new HashMap<>();
		if (StringUtils.isNotBlank(defaultNamespace)) {
			defaultGlobalFlags.put("--namespace", defaultNamespace);
		}
		this.defaultGlobalFlags = Map.copyOf(defaultGlobalFlags);
	}

	// visibleForTesting
	void setExecutor(ICommandExecutor executor) {
		this.executor = new DelegatingCommandExecutor(executor);
	}

	@Override
	public String version() {
		return executor.execute("helm", "version", "--short");
	}

	/**
	 * Installs the helm chart.
	 *
	 * Writes the values into a temporary file if they were not initialized from a File.
	 *
	 * @param name
	 * @param chart
	 * @param values
	 * @param namespace
	 * @param dryRun
	 * @return
	 */
	@Override
	public InstallStatus install(String name, String chart, Values values, String namespace, boolean dryRun) {
		final String[] command = initCommand("helm", "install", name, chart)
				.withFlag("--namespace", namespace)
				.withFlag("-f", values.getValuesFilePath().orElse(writeValuesToTemporaryFile(values, name).getAbsolutePath()))
				.withFlag("--dry-run", Boolean.toString(dryRun))
				.withFlag("-o", "json")
				.build();
		return executor.executeWithJsonOutput(InstallStatus.class, command);
	}

	@Override
	public List<Release> list(String namespace, String filter) {
		final String[] command = initCommand("helm", "list")
				.withFlag("-o", "json")
				.withFlag("--namespace", namespace)
				.withFlag("--filter", filter)
				.withFlag("--time-format", "2006-01-02T15:04:05Z07:00")
				.build();
		return executor.executeWithJsonOutput(new TypeToken<List<Release>>() {
		}.getType(), command);
	}

	@Override
	public List<Release> listInAllNamespaces(String filter) {
		final String[] command = initCommand("helm", "list")
				.withFlag("-o", "json")
				.withFlag("--all-namespaces", "true")
				.withFlag("--filter", filter)
				.withFlag("--time-format", "2006-01-02T15:04:05Z07:00")
				.build();
		return executor.executeWithJsonOutput(new TypeToken<List<Release>>() {
		}.getType(), command);
	}

	@Override
	public void addRepo(String name, String url, String username, String password) {
		var out = executor.execute("helm", "repo", "add", name, url, "--username=" + username, "--password=" + password);
		log.info(out);
	}

	@Override
	public List<Repository> listRepos() {
		return executor.executeWithJsonOutput(new TypeToken<List<Repository>>() {
		}.getType(), "helm", "repo", "list", "-ojson");
	}

	@Override
	public void removeRepo(String name) {
		var out = executor.execute("helm", "repo", "remove", name);
		log.info(out);
	}

	@Override
	public void updateRepos() {
		executor.execute("helm", "repo", "update");
	}

	@Override
	public List<Chart> searchHub(String query) {
		final String[] command = initCommand("helm", "search", "hub", query)
				.withFlag("-o", "json")
				.build();
		return executor.executeWithJsonOutput(new TypeToken<List<Chart>>() {
		}.getType(), command);
	}

	@Override
	public List<Chart> searchRepo(String query, boolean versions, boolean devel) {
		final String[] command = initCommand("helm", "search", "repo", query)
				.withFlag("--versions", Boolean.toString(versions))
				.withFlag("--devel", Boolean.toString(devel))
				.withFlag("-o", "json")
				.build();
		return executor.executeWithJsonOutput(new TypeToken<List<Chart>>() {
		}.getType(), command);
	}

	@Override
	public Status status(String releaseName, String namespace) {
		final String[] command = initCommand("helm", "status", releaseName)
				.withFlag("--namespace", namespace)
				.withFlag("-o", "json")
				.build();
		return executor.executeWithJsonOutput(Status.class, command);
	}

	@Override
	public void uninstall(String name, String namespace, boolean dryRun) {
		final String[] command = initCommand("helm", "uninstall", name)
				.withFlag("--namespace", namespace)
				.withFlag("--dry-run", Boolean.toString(dryRun))
				.build();
		var out = executor.execute(command);
		log.info(out);
	}

	private Command initCommand(String... command) {
		return new Command(command);
	}

	@Getter
	private class Command {
		private String[] command;
		Map<String, String> commandFlags = new LinkedHashMap<>();

		public Command(String... command) {
			this.command = command;
		}

		/**
		 * Flags with null values are ignored.
		 *
		 * @param flag
		 * @param value
		 * @return
		 */
		public Command withFlag(String flag, String value) {
			if (value != null) {
				commandFlags.put(flag, value);
			}
			return this;
		}

		public String[] build() {
			Map<String, String> flags = new LinkedHashMap<>(defaultGlobalFlags);
			flags.putAll(commandFlags);
			final String[] flagsArray = flags.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).toArray(String[]::new);
			return ArrayUtils.addAll(command, flagsArray);
		}
	}

	private File writeValuesToTemporaryFile(Values values, String releaseName) {
		try {
			final Path valuesFile = Files.createTempFile("java-helm-api-" + releaseName, ".yaml");
			Files.writeString(valuesFile, values.asYamlString());
			return valuesFile.toFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
