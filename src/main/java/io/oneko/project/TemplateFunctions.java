package io.oneko.project;

import org.apache.commons.lang3.StringUtils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TemplateFunctions {

	public static String lowerCase(String str) {
		return StringUtils.lowerCase(str);
	}

	public static String replace(String text, String searchString, String replacement) {
		return StringUtils.replace(text, searchString, replacement);
	}

	public static String replace(String text, String searchString, String replacement, int max) {
		return StringUtils.replace(text, searchString, replacement, max);
	}

	public static String trim(String str) {
		return StringUtils.trim(str);
	}

	public static String upperCase(String str) {
		return StringUtils.upperCase(str);
	}

	public static String truncate(String str, int maxWidth) {
		return StringUtils.truncate(str, maxWidth);
	}

	public static String removeEnd(final String str, final String remove) {
		return StringUtils.removeEnd(str, remove);
	}

	public static String removeStart(final String str, final String remove) {
		return StringUtils.removeStart(str, remove);
	}

}
