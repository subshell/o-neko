package io.oneko.project.persistence;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.oneko.templates.ConfigurationTemplate;
import io.oneko.templates.ReadableConfigurationTemplate;
import io.oneko.templates.WritableConfigurationTemplate;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConfigurationTemplateMongoMapper {

	public static ConfigurationTemplateMongo toConfigurationTemplateMongo(ConfigurationTemplate configurationTemplate) {
		return ConfigurationTemplateMongo.builder()
				.id(configurationTemplate.getId())
				.name(configurationTemplate.getName())
				.content(configurationTemplate.getContent())
				.description(configurationTemplate.getDescription())
				.chartName(configurationTemplate.getChartName())
				.chartVersion(configurationTemplate.getChartVersion())
				.helmRegistryId(configurationTemplate.getHelmRegistryId())
				.build();
	}

	public static List<ConfigurationTemplateMongo> toConfigurationTemplateMongos(Collection<WritableConfigurationTemplate> configurationTemplates) {
		return configurationTemplates.stream()
				.map(ConfigurationTemplateMongoMapper::toConfigurationTemplateMongo)
				.collect(Collectors.toList());
	}

	public static ReadableConfigurationTemplate fromConfigurationTemplateMongo(ConfigurationTemplateMongo templateMongo) {
		return ReadableConfigurationTemplate.builder()
				.id(templateMongo.getId())
				.name(templateMongo.getName())
				.content(templateMongo.getContent())
				.description(templateMongo.getDescription())
				.chartName(templateMongo.getChartName())
				.chartVersion(templateMongo.getChartVersion())
				.helmRegistryId(templateMongo.getHelmRegistryId())
				.build();
	}

	public static List<ReadableConfigurationTemplate> fromConfigurationTemplateMongos(Collection<ConfigurationTemplateMongo> mongos) {
		return mongos.stream()
				.map(ConfigurationTemplateMongoMapper::fromConfigurationTemplateMongo)
				.collect(Collectors.toList());
	}
}
