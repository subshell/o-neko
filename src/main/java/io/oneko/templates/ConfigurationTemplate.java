package io.oneko.templates;

import java.util.UUID;

public interface ConfigurationTemplate {
	UUID getId();
	/**
	 * configuration template to be used for versions. Should be a multi line yaml-string.
	 */
	String getContent();

	String getName();

	String getDescription();

	String getChartName();

	String getChartVersion();

	UUID getHelmRegistryId();

}
