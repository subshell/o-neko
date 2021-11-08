package io.oneko.automations;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LifetimeBehaviourTest {

	@Test
	void days() {
		LifetimeBehaviour lb = LifetimeBehaviour.ofDays(7);
		assertThat(lb.getType()).isEqualTo(LifetimeBehaviourType.DAYS);
		assertThat(lb.getValue()).isEqualTo(7);
	}


	@Test
	void infinite() {
		LifetimeBehaviour lb = LifetimeBehaviour.infinite();
		assertThat(lb.isInfinite()).isTrue();
		assertThat(lb.getType()).isEqualTo(LifetimeBehaviourType.INFINITE);
	}

	@Test
	void untilWeekend() {
		LifetimeBehaviour lb = LifetimeBehaviour.untilWeekend();
		assertThat(lb.isUntilWeekend()).isTrue();
		assertThat(lb.getType()).isEqualTo(LifetimeBehaviourType.UNTIL_WEEKEND);
	}

	@Test
	void UntilTonight() {
		LifetimeBehaviour lb = LifetimeBehaviour.untilTonight();
		assertThat(lb.isUntilTonight()).isTrue();
		assertThat(lb.getType()).isEqualTo(LifetimeBehaviourType.UNTIL_TONIGHT);
	}
}
