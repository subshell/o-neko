package io.oneko.templates;

import com.google.common.base.Preconditions;

import java.util.*;

public class ConfigurationTemplates {

	/**
	 * Ensures that the given collection of templates does not contain an ID or name twice.
	 */
	public static void ensureConsistentCollection(Collection<? extends ConfigurationTemplate> templates) {
		ensureNoDuplicate(templates);
		ensureNoDuplicateNames(templates);
	}

	/**
	 * Ensures, that the given collection of configuration templates does not contain a single configuration twice.
	 */
	public static void ensureNoDuplicate(Collection<? extends ConfigurationTemplate> templates) {
		Set<UUID> ids = new HashSet<>(templates.size());
		for (ConfigurationTemplate template : templates) {
			Preconditions.checkArgument(ids.add(template.getId()), "Collection of configuration templates contains the template %s twice.", template.getId());
		}
	}

	/**
	 * Ensures, that the given collection of configuration templates does not contain two configurations with the same name.
	 */
	public static void ensureNoDuplicateNames(Collection<? extends ConfigurationTemplate> templates) {
		Set<String> names = new HashSet<>(templates.size());
		for (ConfigurationTemplate template : templates) {
			Preconditions.checkArgument(names.add(template.getName()), "Collection of configuration templates contains the name %s twice.", template.getName());
		}
	}

	public static List<ConfigurationTemplate> unifyTemplateSets(Collection<Collection<? extends ConfigurationTemplate>> templateSets) {
		Map<String, ConfigurationTemplate> templateNameToTemplateMap = new LinkedHashMap<>();
		for (Collection<? extends ConfigurationTemplate> templateSet : templateSets) {
			for (ConfigurationTemplate template : templateSet) {
				templateNameToTemplateMap.put(template.getName(), template);
			}
		}
		return new ArrayList<>(templateNameToTemplateMap.values());
	}

	@SafeVarargs
	public static List<ConfigurationTemplate> unifyTemplateSets(Collection<? extends ConfigurationTemplate>... templateSets) {
		Map<String, ConfigurationTemplate> templateNameToTemplateMap = new LinkedHashMap<>();
		for (Collection<? extends ConfigurationTemplate> templateSet : templateSets) {
			for (ConfigurationTemplate template : templateSet) {
				templateNameToTemplateMap.put(template.getName(), template);
			}
		}
		return new ArrayList<>(templateNameToTemplateMap.values());
	}
}
