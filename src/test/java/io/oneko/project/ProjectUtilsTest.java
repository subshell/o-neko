package io.oneko.project;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class ProjectUtilsTest {

	@Test
	void testMatchesUrls() {
		var urls = List.of("foo.bar.baz", "https://my-funny-url.com", "http://other-url.com");

		SoftAssertions softly = new SoftAssertions();

		softly.assertThat(ProjectUtils.anyUrlMatches(urls, "foo.bar.baz")).isTrue();
		softly.assertThat(ProjectUtils.anyUrlMatches(urls, "http://foo.bar.baz")).isTrue();
		softly.assertThat(ProjectUtils.anyUrlMatches(urls, "https://foo.bar.baz")).isTrue();
		softly.assertThat(ProjectUtils.anyUrlMatches(urls, "http://my-funny-url.com")).isTrue();
		softly.assertThat(ProjectUtils.anyUrlMatches(urls, "https://other-url.com/random/path")).isTrue();

		softly.assertThat(ProjectUtils.anyUrlMatches(urls, "not-matching.foo.bar.baz")).isFalse();

		softly.assertAll();
	}

}
