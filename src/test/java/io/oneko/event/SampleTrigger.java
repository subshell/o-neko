package io.oneko.event;

import io.oneko.activity.ActivityPriority;

public class SampleTrigger extends EventTrigger {
	@Override
	public String humanReadable() {
		return "SampleTrigger";
	}

	@Override
	public ActivityPriority priority() {
		return ActivityPriority.INFO;
	}

	@Override
	public String getType() {
		return "sample";
	}
}
