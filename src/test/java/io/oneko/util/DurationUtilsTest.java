package io.oneko.util;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class DurationUtilsTest {

	@Test
	void testIsLongerThan() {
		var a = Duration.ofMinutes(5);
		var b = Duration.ofDays(5);

		assertThat(DurationUtils.isLongerThan(a, b)).isFalse();
		assertThat(DurationUtils.isLongerThan(b, a)).isTrue();
	}

	@Test
	void testIsShorterThan() {
		var a = Duration.ofMinutes(5);
		var b = Duration.ofDays(5);

		assertThat(DurationUtils.isShorterThan(a, b)).isTrue();
		assertThat(DurationUtils.isShorterThan(b, a)).isFalse();
	}

}
