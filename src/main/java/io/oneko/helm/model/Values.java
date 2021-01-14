package io.oneko.helm.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.Yaml;

import lombok.Getter;

@Getter
public class Values {

	private final Map<String, Object> values;

	private Values(Map<String, Object> values) {
		this.values = Collections.unmodifiableMap(new TreeMap<>(values)); // keeping a deterministic order of the arguments helps us with testing
	}

	public static Values fromYamlString(String yamlString) {
		return new Values(parseValuesFromYaml(yamlString));
	}

	public static Values fromFile(String pathToYamlFile) {
		return fromFile(new File(pathToYamlFile));
	}

	public static Values fromFile(File yamlFile) {
		return new Values(readValuesFromFile(yamlFile));
	}

	public static Values fromMap(Map<String, Object> values) {
		return new Values(values);
	}

	public String asString() {
		return getValues().entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(","));
	}

	private static Map<String, Object> readValuesFromFile(File yamlFile) {
		Yaml yaml = new Yaml();
		try {
			return flatten(yaml.load(new FileInputStream(yamlFile)));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static Map<String, Object> parseValuesFromYaml(String yamlString) {
		Yaml yaml = new Yaml();
		return flatten(yaml.load(yamlString));
	}

	private static Map<String, Object> flatten(Map<String, Object> source) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (String key : source.keySet()) {
			Object value = source.get(key);
			if (value instanceof Map) {
				Map<String, Object> nestedMap = flatten((Map<String, Object>) value);
				for (String nestedKey : nestedMap.keySet()) {
					result.put(key + "." + nestedKey, nestedMap.get(nestedKey));
				}
			} else if (value instanceof Collection) {
				StringBuilder sb = new StringBuilder();
				String separator = "";
				sb.append("{");
				for (Object element : ((Collection<?>) value)) {
					Map<String, Object> subMap = flatten(Map.of(key, element));
					sb.append(separator)
							.append(subMap.entrySet().iterator().next().getValue().toString());
					separator = ",";
				}
				sb.append("}");
				result.put(key, sb.toString());
			} else {
				result.put(key, value);
			}
		}
		return result;
	}
}
