package io.oneko.helm.api;

import static io.oneko.helm.api.HelmTestStrings.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.oneko.helm.model.Chart;
import io.oneko.helm.model.Values;

class HelmTest {

	private Helm uut;

	@BeforeEach
	void setup() {
		final TestCommandExecutor tce = new TestCommandExecutor();

		// This will cause tests to fail if the command invoked changes.
		// If this happens please check that the command produces identical output and then fix the command here
		tce.addDefinedCommand(helm_repo_list, "helm repo list -ojson");
		tce.addDefinedCommand(helm_list_all_namespaces, "helm list -o=json --all-namespaces=true --time-format=2006-01-02T15:04:05Z07:00");
		tce.addDefinedCommand(helm_status_oneko, "helm status oneko -o=json");
		tce.addDefinedCommand(helm_search_repo_sophora, "helm search repo Sophora Server -o=json");
		tce.addDefinedCommand(helm_list, "helm list -o=json --time-format=2006-01-02T15:04:05Z07:00");
		tce.addDefinedCommand("v3.4.2+g23dd3af", "helm version --short");
		tce.addDefinedCommand(helm_install, "helm install ontest o-neko --namespace=deletethispls --set=ingress.host=oneko.subshell.cloud,oneko.config.spring.data.mongodb.autoIndexCreation=true,oneko.credentialsCoderKeySecret.name=o-neko-credentials-coder-key,oneko.mongodb.secret.name=mongodb-credentials,resources.limits.cpu=1.5,resources.limits.memory=6G,resources.requests.cpu=0.5,resources.requests.memory=2G,serviceAccountName=o-neko-sa --dry-run=false -o=json");

		uut = new Helm(tce);
	}

	@Test
	void install() {
		var result = uut.install("ontest", "o-neko", Values.fromYamlString(ONEKO_VALUES), "deletethispls");
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
