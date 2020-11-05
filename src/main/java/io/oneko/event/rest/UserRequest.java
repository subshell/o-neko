package io.oneko.event.rest;

import io.oneko.activity.ActivityPriority;
import io.oneko.event.EventTrigger;
import lombok.Getter;

/**
 * This represents a request to the REST API (by a certain user) as source of events.
 */
@Getter
public class UserRequest extends EventTrigger {

	public static final String TYPE = "UserRequest";

	private final String userName;

	public UserRequest(String userName) {
		this.userName = userName;
	}

	@Override
	public String humanReadable() {
		return "HTTP-Request by user " + getUserName();
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
