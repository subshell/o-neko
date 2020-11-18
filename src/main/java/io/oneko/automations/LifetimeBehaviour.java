package io.oneko.automations;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.oneko.kubernetes.deployments.Deployment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class LifetimeBehaviour {

	/**
	 * 0 == Infinite
	 */
	private final int daysToLive;

	public static LifetimeBehaviour infinite() {
		return new LifetimeBehaviour(0);
	}

	public static LifetimeBehaviour ofDays(int daysToLive) {
		return new LifetimeBehaviour(daysToLive);
	}

	@JsonIgnore
	public boolean isInfinite() {
		return daysToLive == 0;
	}

	@JsonIgnore
	public boolean isExpired(Instant timestamp) {
		if (timestamp == null) {
			return isInfinite();
		}
		return !isInfinite() && !Instant.now().minus(daysToLive, ChronoUnit.DAYS).isBefore(timestamp);
	}

	@JsonIgnore
	public boolean isExpired(Deployment deployment) {
		return isExpired(deployment.getTimestamp().orElse(null));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LifetimeBehaviour)) return false;
		LifetimeBehaviour that = (LifetimeBehaviour) o;
		return getDaysToLive() == that.getDaysToLive();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getDaysToLive());
	}
}
