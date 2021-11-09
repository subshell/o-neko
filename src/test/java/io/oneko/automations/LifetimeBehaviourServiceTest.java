package io.oneko.automations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.oneko.utils.TimeMachine;

class LifetimeBehaviourServiceTest {

	final TimeMachine clock = new TimeMachine();

	@BeforeEach
	void setup() {
		clock.timeTravelTo(Instant.EPOCH);
	}

	@Test
	void testInfiniteLifetime() {
		LifetimeBehaviourService uut = createLifetimeBehaviourService(23, 59, 0);
		LifetimeBehaviour lb = LifetimeBehaviour.infinite();

		assertThat(uut.isExpired(lb, clock.instant().minus(5, ChronoUnit.MINUTES))).isFalse();
		assertThat(uut.isExpired(lb, clock.instant().plus(5, ChronoUnit.MINUTES))).isFalse();
	}

	@Test
	void testFiniteLifetime() {
		LifetimeBehaviourService uut = createLifetimeBehaviourService(23, 59, 0);
		LifetimeBehaviour lb = LifetimeBehaviour.ofDays(5);

		assertThat(uut.isExpired(lb, clock.instant())).isFalse();
		assertThat(uut.isExpired(lb, clock.instant().minus(4, ChronoUnit.DAYS))).isFalse();
		assertThat(uut.isExpired(lb, clock.instant().minus(6, ChronoUnit.DAYS))).isTrue();
		assertThat(uut.isExpired(lb, clock.instant().minus(7, ChronoUnit.DAYS))).isTrue();
	}

	@Test
	void testUntilTonightNotExpired() {
		LifetimeBehaviourService uut = createLifetimeBehaviourService(23, 59, 0);
		LifetimeBehaviour lb = LifetimeBehaviour.untilTonight();
		assertThat(uut.isExpired(lb, clock.instant())).isFalse();
	}

	@Test
	void testUntilTonightExpired() {
		LifetimeBehaviourService uut = createLifetimeBehaviourService(23, 59, 0);
		LifetimeBehaviour lb = LifetimeBehaviour.untilTonight();
		assertThat(uut.isExpired(lb, clock.instant().minus(1, ChronoUnit.DAYS))).isTrue();
	}

	@Test
	void testUntilTonightExpiredNextDay() {
		LifetimeBehaviourService uut = createLifetimeBehaviourService(5, 0, 1);
		LifetimeBehaviour lb = LifetimeBehaviour.untilTonight();
		assertThat(uut.isExpired(lb, clock.instant().minus(1, ChronoUnit.DAYS))).isFalse();
		assertThat(uut.isExpired(lb, clock.instant().minus(2, ChronoUnit.DAYS))).isTrue();
	}

	@Test
	void untilTonightOnSameDay() {
		LifetimeBehaviourService uut = createLifetimeBehaviourService(3, 0, 0);
		LifetimeBehaviour lb = LifetimeBehaviour.untilTonight();

		clock.timeTravelTo(Instant.EPOCH.plus(4, ChronoUnit.HOURS));

		assertThat(uut.isExpired(lb, Instant.EPOCH)).isTrue();
		assertThat(uut.isExpired(lb, clock.instant().plus(1, ChronoUnit.HOURS))).isFalse();
	}

	@Test
	void testUntilWeekendNotExpired() {
		LifetimeBehaviourService uut = createLifetimeBehaviourService(23, 59, 0);
		LifetimeBehaviour lb = LifetimeBehaviour.untilWeekend();
		assertThat(uut.isExpired(lb, clock.instant())).isFalse();
	}

	@Test
	void testUntilWeekendExpired() {
		// Current day is Jan 1st, 1970 is on Thursday
		LifetimeBehaviourService uut = createLifetimeBehaviourService(23, 59, 0);
		LifetimeBehaviour lb = LifetimeBehaviour.untilWeekend();

		assertThat(uut.isExpired(lb, clock.instant().minus(6, ChronoUnit.DAYS))).isFalse();
		// Last Thursday was 7 days ago
		assertThat(uut.isExpired(lb, clock.instant().minus(7, ChronoUnit.DAYS))).isTrue();
		assertThat(uut.isExpired(lb, clock.instant().minus(8, ChronoUnit.DAYS))).isTrue();
	}

	@Test
	void testUntilWeekendExpiredCustomEndOfWeek() {
		// Current day is Jan 1st, 1970 is on Thursday
		LifetimeProperties.EndOfDay endOfDay = new LifetimeProperties.EndOfDay(23, 59, 0);
		LifetimeProperties properties = new LifetimeProperties(endOfDay, "saturday");
		LifetimeBehaviourService uut = new LifetimeBehaviourService(properties, clock);
		LifetimeBehaviour lb = LifetimeBehaviour.untilWeekend();

		assertThat(uut.isExpired(lb, clock.instant().minus(5, ChronoUnit.DAYS))).isFalse();
		// Last Friday was 6 days ago
		assertThat(uut.isExpired(lb, clock.instant().minus(6, ChronoUnit.DAYS))).isTrue();
		assertThat(uut.isExpired(lb, clock.instant().minus(7, ChronoUnit.DAYS))).isTrue();
	}

	@Test
	void rejectInvalidWeekdays() {
		LifetimeProperties.EndOfDay endOfDay = new LifetimeProperties.EndOfDay(23, 59, 0);
		LifetimeProperties properties = new LifetimeProperties(endOfDay, "someday");
		LifetimeBehaviourService uut = new LifetimeBehaviourService(properties, clock);
		assertThatThrownBy(uut::checkDayOfTheWeek).isInstanceOf(IllegalArgumentException.class);
	}

	private LifetimeBehaviourService createLifetimeBehaviourService(int hour, int minute, int dayOffset) {
		LifetimeProperties.EndOfDay endOfDay = new LifetimeProperties.EndOfDay(hour, minute, dayOffset);
		LifetimeProperties properties = new LifetimeProperties(endOfDay, "friday");
		return new LifetimeBehaviourService(properties, clock);
	}
}