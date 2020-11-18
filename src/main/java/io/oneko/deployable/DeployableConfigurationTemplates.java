package io.oneko.deployable;

import io.oneko.templates.ConfigurationTemplate;
import lombok.Data;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class DeployableConfigurationTemplates {

	private final Set<DeployableConfigurationTemplate> templates;

	public static DeployableConfigurationTemplates of(Collection<? extends ConfigurationTemplate> configurationTemplates) {
		final Set<DeployableConfigurationTemplate> deployableTemplates = configurationTemplates.stream()
				.map(template -> new DeployableConfigurationTemplate(template.getContent(), template.getName()))
				.collect(Collectors.toSet());
		return new DeployableConfigurationTemplates(deployableTemplates);
	}

}
