package io.oneko.helmapi.process;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

class DelegatingCommandExecutorTest {

	@Test
	void leadingOciPullProgressLinesBeforeTheJsonAreIgnored() {
		ICommandExecutor delegate = command -> "Pulled: localhost:5001/helm-charts/demo:0.1.0\n"
				+ "Digest: sha256:bdffdbc74f05ddfd83f08afaa12811455f893a5be4e6612bd32baff2d7b049bb\n"
				+ "{\"name\":\"ontest\"}";
		var uut = new DelegatingCommandExecutor(delegate);

		var result = uut.executeWithJsonOutput(Map.class, "helm", "install");

		assertThat(result).containsEntry("name", "ontest");
	}

	@Test
	void jsonArrayOutputIsStillParsedCorrectly() {
		ICommandExecutor delegate = command -> "[{\"name\":\"a\"},{\"name\":\"b\"}]";
		var uut = new DelegatingCommandExecutor(delegate);

		var result = uut.executeWithJsonOutput(java.util.List.class, "helm", "list");

		assertThat(result).hasSize(2);
	}

	@Test
	void nonJsonOutputStillFailsWithTheRawOutputAttached() {
		ICommandExecutor delegate = command -> "Error: something went wrong, but somehow exited 0";
		var uut = new DelegatingCommandExecutor(delegate);

		assertThatThrownBy(() -> uut.executeWithJsonOutput(Map.class, "helm", "install"))
				.isInstanceOf(CommandException.class)
				.hasMessageContaining("Error: something went wrong, but somehow exited 0");
	}

}
