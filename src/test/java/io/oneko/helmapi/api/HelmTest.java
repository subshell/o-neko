package io.oneko.helmapi.api;

import static io.oneko.helmapi.api.HelmTestStrings.*;
import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.oneko.helmapi.model.Chart;
import io.oneko.helmapi.model.Values;

class HelmTest {

	private Helm uut;
	private File tempFile;

	@BeforeEach
	void setup() throws IOException {
		final TestCommandExecutor tce = new TestCommandExecutor();

		// This will cause tests to fail if the command invoked changes.
		// If this happens please check that the command produces identical output and then fix the command here
		tempFile = Files.createTempFile("helmapitestvalues", "yaml").toFile();
		tce.addDefinedCommand(helm_repo_list, "helm repo list -ojson");
		tce.addDefinedCommand(helm_list_all_namespaces, "helm list -o=json --all-namespaces=true --time-format=2006-01-02T15:04:05Z07:00");
		tce.addDefinedCommand(helm_status_oneko, "helm status oneko -o=json");
		tce.addDefinedCommand(helm_search_repo_sophora, "helm search repo Sophora Server --versions=false --devel=false -o=json");
		tce.addDefinedCommand(helm_list, "helm list -o=json --time-format=2006-01-02T15:04:05Z07:00");
		tce.addDefinedCommand("v3.4.2+g23dd3af", "helm version --short");
		tce.addDefinedCommand(helm_install, "helm install ontest o-neko --namespace=deletethispls -f=" + tempFile.getAbsolutePath() + " --dry-run=false --wait=false -o=json");

		uut = new Helm(tce);
	}

	@Test
	void install() throws IOException {
		Files.writeString(tempFile.toPath(), ONEKO_VALUES);
		var result = uut.install("ontest", "o-neko", Values.fromFile(tempFile), "deletethispls");
		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo("ontest");
		assertThat(result.getNamespace()).isEqualTo("deletethispls");
		assertThat(result.getChart().getMetadata().getName()).isEqualTo("o-neko");
	}

	@Test
	void version() {
		assertThat(uut.version()).isEqualTo("v3.4.2+g23dd3af");
	}

	@Test
	void repoList() {
		var result = uut.listRepos();
		assertThat(result).hasSize(3);
	}

	@Test
	void list() {
		var result = uut.list();
		assertThat(result).hasSize(1);
	}

	@Test
	void listInAllNamespaces() {
		var result = uut.listInAllNamespaces();
		assertThat(result).hasSize(6);
	}

	@Test
	void status() {
		var status = uut.status("oneko");
		assertThat(status).isNotNull();
	}

	@Test
	void search() {
		var result = uut.searchRepo("Sophora Server");
		var expectedChart = new Chart("sophora/sophora-server", "0.1.0", "4", "A Helm chart for the Sophora Server");
		assertThat(result).hasSize(1).element(0).isEqualTo(expectedChart);
	}

}
