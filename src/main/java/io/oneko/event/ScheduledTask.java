package io.oneko.event;

import io.oneko.activity.ActivityPriority;
import lombok.Getter;

@Getter
public class ScheduledTask extends EventTrigger {
	public static final String TYPE = "ScheduledTask";

	private final String taskName;

	public ScheduledTask(String taskName) {
		this.taskName = taskName;
	}

	@Override
	public String humanReadable() {
		return "ScheduledTask " + taskName;
	}

	@Override
	public ActivityPriority priority() {
		return ActivityPriority.INFO;
	}

	@Override
	public String getType() {
		return TYPE;
	}
}
