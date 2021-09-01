package io.oneko.project;

import java.util.Collection;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

public class ProjectUtils {

	private ProjectUtils() {
	}

	public static boolean anyUrlMatches(Collection<String> urls, String candidate) {
		var candidateUrlWithoutProtocol = RegExUtils.removeAll(candidate, "^https?:\\/\\/");
		return urls.stream()
				.anyMatch(url -> {
					final String urlWithoutProtocol = RegExUtils.removeAll(url, "^https?:\\/\\/");
					return StringUtils.startsWith(candidateUrlWithoutProtocol, urlWithoutProtocol);
				});
	}

}
