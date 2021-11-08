package io.oneko.automations;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class LifetimeBehaviour {

	private final LifetimeBehaviourType type;
	private final Integer value;

	public LifetimeBehaviour(LifetimeBehaviourType type, Integer value) {
		this.type = type == null ? LifetimeBehaviourType.DAYS : type;
		this.value = value == null ? 0 : value;
	}

	public static LifetimeBehaviour infinite() {
		return new LifetimeBehaviour(LifetimeBehaviourType.INFINITE, 0);
	}

	public static LifetimeBehaviour ofDays(int daysToLive) {
		return new LifetimeBehaviour(LifetimeBehaviourType.DAYS, daysToLive);
	}

	public static LifetimeBehaviour untilTonight() {
		return new LifetimeBehaviour(LifetimeBehaviourType.UNTIL_TONIGHT, 0);
	}

	public static LifetimeBehaviour untilWeekend() {
		return new LifetimeBehaviour(LifetimeBehaviourType.UNTIL_WEEKEND, 0);
	}

	@JsonIgnore
	public boolean isInfinite() {
		return type == LifetimeBehaviourType.INFINITE;
	}

	@JsonIgnore
	public boolean isUntilTonight() {
		return type == LifetimeBehaviourType.UNTIL_TONIGHT;
	}

	@JsonIgnore
	public boolean isUntilWeekend() {
		return type == LifetimeBehaviourType.UNTIL_WEEKEND;
	}

}
