package io.oneko.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Preconditions;

public class ConfigurationTemplates {

	/**
	 * Ensures that the given collection of templates does not contain an ID or name twice.
	 */
	public static void ensureConsistentCollection(Collection<WritableConfigurationTemplate> templates) {
		ensureNoDuplicate(templates);
		ensureNoDuplicateNames(templates);
	}

	/**
	 * Ensures, that the given collection of configuration templates does not contain a single configuration twice.
	 */
	public static void ensureNoDuplicate(Collection<WritableConfigurationTemplate> templates) {
		Set<UUID> ids = new HashSet<>(templates.size());
		for (WritableConfigurationTemplate template : templates) {
			Preconditions.checkArgument(ids.add(template.getId()), "Collection of configuration templates contains the template %s twice.", template.getId());
		}
	}

	/**
	 * Ensures, that the given collection of configuration templates does not contain two configurations with the same name.
	 */
	public static void ensureNoDuplicateNames(Collection<WritableConfigurationTemplate> templates) {
		Set<String> names = new HashSet<>(templates.size());
		for (WritableConfigurationTemplate template : templates) {
			Preconditions.checkArgument(names.add(template.getName()), "Collection of configuration templates contains the name %s twice.", template.getName());
		}
	}

	@SafeVarargs
	public static List<ConfigurationTemplate> unifyTemplateSets(Collection<ConfigurationTemplate>... templateSets) {
		Map<String, ConfigurationTemplate> templateNameToTemplateMap = new LinkedHashMap<>();
		for (Collection<ConfigurationTemplate> templateSet : templateSets) {
			for (ConfigurationTemplate template : templateSet) {
				templateNameToTemplateMap.put(template.getName(), template);
			}
		}
		return new ArrayList<>(templateNameToTemplateMap.values());
	}
}
