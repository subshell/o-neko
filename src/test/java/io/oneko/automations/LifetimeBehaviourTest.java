package io.oneko.automations;

import static java.time.temporal.ChronoUnit.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class LifetimeBehaviourTest {


	@Test
	void testInfiniteLifetime() {
		LifetimeBehaviour lb = LifetimeBehaviour.infinite();

		assertThat(lb.isInfinite(), is(true));
		assertThat(lb.isExpired(Instant.now().minus(5, MINUTES)), is(false));
		assertThat(lb.isExpired(Instant.now().plus(5, MINUTES)), is(false));
	}

	@Test
	void testFiniteLifetime() {
		LifetimeBehaviour lb = LifetimeBehaviour.ofDays(5);

		assertThat(lb.isInfinite(), is(false));
		assertThat(lb.isExpired(Instant.now()), is(false));
		assertThat(lb.isExpired(Instant.now().minus(4, DAYS)), is(false));
		assertThat(lb.isExpired(Instant.now().minus(5, DAYS)), is(true));
		assertThat(lb.isExpired(Instant.now().minus(6, DAYS)), is(true));
	}

}
