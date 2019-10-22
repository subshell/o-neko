package io.oneko.event;

import io.oneko.activity.ActivityPriority;

public class UnknownTrigger extends EventTrigger {

	public static final UnknownTrigger INSTANCE = new UnknownTrigger();

	private UnknownTrigger() {
	}

	@Override
	public String humanReadable() {
		return "unknown";
	}

	@Override
	public ActivityPriority priority() {
		return ActivityPriority.INFO;
	}

	@Override
	public String getType() {
		return "unknown";
	}
}
