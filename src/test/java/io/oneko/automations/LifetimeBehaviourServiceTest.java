package io.oneko.automations;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

class LifetimeBehaviourServiceTest {

	final Clock clock = Clock.fixed(Instant.EPOCH, ZoneId.systemDefault());

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
	void testUntilWeekendNotExpired() {
		LifetimeBehaviourService uut = createLifetimeBehaviourService(23, 59, 0);
		LifetimeBehaviour lb = LifetimeBehaviour.untilWeekend();
		assertThat(uut.isExpired(lb, clock.instant())).isFalse();
	}

	@Test
	void testUntilWeekendExpired() {
		LifetimeBehaviourService uut = createLifetimeBehaviourService(23, 59, 0);
		LifetimeBehaviour lb = LifetimeBehaviour.untilWeekend();
		assertThat(uut.isExpired(lb, clock.instant().minus(8, ChronoUnit.DAYS))).isTrue();
	}

	private LifetimeBehaviourService createLifetimeBehaviourService(int hour, int minute, int dayOffset) {
		LifetimeProperties.EndOfDay endOfDay = new LifetimeProperties.EndOfDay(hour, minute, dayOffset);
		LifetimeProperties properties = new LifetimeProperties(endOfDay, "friday");
		return new LifetimeBehaviourService(properties, clock);
	}
}