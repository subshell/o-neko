package io.oneko.util;

import java.time.Duration;

public class DurationUtils {

	private DurationUtils() {
	}

	public static boolean isShorterThan(Duration isThisShorter, Duration thanThis) {
		return isThisShorter.compareTo(thanThis) < 0;
	}

	public static boolean isLongerThan(Duration isThisLonger, Duration thanThis) {
		return isThisLonger.compareTo(thanThis) > 0;
	}
}
