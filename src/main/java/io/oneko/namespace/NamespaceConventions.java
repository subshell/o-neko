package io.oneko.namespace;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class NamespaceConventions {
	public static final String NAMESPACE_REGEX = "^[a-z0-9]([-a-z0-9]*[a-z0-9])?$";
	public static final String NAMESPACE_PREFIX = "on-";

	public static String sanitizeNamespace(String namespace) {
		String candidate = namespace.toLowerCase();
		candidate = candidate.replaceAll("_", "-");//for readability replace all underscores with a dash
		candidate = candidate.replaceAll("[^a-z0-9\\-]", StringUtils.EMPTY);//remove invalid chars (only alphanumeric and dash allowed)
		candidate = candidate.replaceAll("^[\\-]*", StringUtils.EMPTY);//remove invalid start (remove dot, dash and underscore from start)
		candidate = candidate.substring(0, Math.min(candidate.length(), 63 - NAMESPACE_PREFIX.length()));//restrict size
		candidate = candidate.replaceAll("[\\-]*$", StringUtils.EMPTY);//remove invalid end (remove dot, dash and underscore from end)
		if (StringUtils.isBlank(candidate)) {
			throw new IllegalArgumentException("Can not create a legal namespace from " + namespace);
		}
		return NAMESPACE_PREFIX + candidate;
	}
}
