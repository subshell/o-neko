package io.oneko.helmapi.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class Values {

	private final String valuesYaml;
	private final File valuesFile;

	private Values(String valuesYaml) {
		this.valuesYaml = valuesYaml; // keeping a deterministic order of the arguments helps us with testing
		this.valuesFile = null;
	}

	private Values(File valuesFile) {
		this.valuesYaml = readValuesFromFile(valuesFile);
		this.valuesFile = valuesFile;
	}

	public static Values fromYamlString(String yamlString) {
		return new Values(yamlString);
	}

	public static Values fromFile(String pathToYamlFile) {
		return fromFile(new File(pathToYamlFile));
	}

	public static Values fromFile(File yamlFile) {
		return new Values(yamlFile);
	}

	public String asYamlString() {
		return valuesYaml;
	}

	public Optional<String> getValuesFilePath() {
		return Optional.ofNullable(valuesFile).map(File::getAbsolutePath);
	}

	private static String readValuesFromFile(File file) {
		try {
			return Files.readString(file.toPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
