package io.oneko.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * A clock implementation that allows you to set the time based on your needs.
 * Use the method {@link #timeTravelTo(Instant)} to set the time to a specific moment in time.
 * Use {@link #timeTravelBackToNow()} to let this clock run again with system time.
 */
public class TimeMachine extends Clock {

	private final ZoneId zone;
	private Clock delegateClock;

	public TimeMachine() {
		this(ZoneId.systemDefault());
	}

	public TimeMachine(ZoneId zone) {
		this.zone = zone;
		setToSystemClock();
	}

	private void setToSystemClock() {
		delegateClock = Clock.system(this.zone);
	}

	public void timeTravelTo(Instant futureNow) {
		this.delegateClock = Clock.fixed(futureNow, ZoneId.systemDefault());
	}

	public void timeTravelBackToNow() {
		setToSystemClock();
	}

	@Override
	public ZoneId getZone() {
		return this.zone;
	}

	@Override
	public Clock withZone(ZoneId zone) {
		return new TimeMachine(zone);
	}

	@Override
	public Instant instant() {
		return this.delegateClock.instant();
	}
}
