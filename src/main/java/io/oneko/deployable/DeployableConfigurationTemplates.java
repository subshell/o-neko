package io.oneko.deployable;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import io.oneko.templates.ConfigurationTemplate;
import lombok.Data;

@Data
public class DeployableConfigurationTemplates {

	private final Set<DeployableConfigurationTemplate> templates;

	public static DeployableConfigurationTemplates of(Collection<ConfigurationTemplate> configurationTemplates) {
		final Set<DeployableConfigurationTemplate> deployableTemplates = configurationTemplates
				.stream()
				.map(template -> new DeployableConfigurationTemplate(template.getContent(), template.getName()))
				.collect(Collectors.toSet());
		return new DeployableConfigurationTemplates(deployableTemplates);
	}

}
