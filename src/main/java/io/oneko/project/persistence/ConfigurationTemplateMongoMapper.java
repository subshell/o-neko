package io.oneko.project.persistence;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.oneko.templates.ConfigurationTemplate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConfigurationTemplateMongoMapper {

	public static ConfigurationTemplateMongo toConfigurationTemplateMongo(ConfigurationTemplate configurationTemplate) {
		return ConfigurationTemplateMongo.builder()
				.id(configurationTemplate.getId())
				.name(configurationTemplate.getName())
				.content(configurationTemplate.getContent())
				.description(configurationTemplate.getDescription())
				.build();
	}

	public static List<ConfigurationTemplateMongo> toConfigurationTemplateMongos(Collection<ConfigurationTemplate> configurationTemplates) {
		return configurationTemplates.stream()
				.map(ConfigurationTemplateMongoMapper::toConfigurationTemplateMongo)
				.collect(Collectors.toList());
	}

	public static ConfigurationTemplate fromConfigurationTemplateMongo(ConfigurationTemplateMongo templateMongo) {
		return ConfigurationTemplate.builder()
				.id(templateMongo.getId())
				.name(templateMongo.getName())
				.content(templateMongo.getContent())
				.description(templateMongo.getDescription())
				.build();
	}

	public static List<ConfigurationTemplate> fromConfigurationTemplateMongos(Collection<ConfigurationTemplateMongo> mongos) {
		return mongos.stream()
				.map(ConfigurationTemplateMongoMapper::fromConfigurationTemplateMongo)
				.collect(Collectors.toList());
	}
}
