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

	public UserRequest() {
		/*
		 * Having the username as a parameter here would totally be dope, but is seems almost impossible to get it in
		 * here from where the user request objects are created.
		 */
	}

	@Override
	public String humanReadable() {
		return "HTTP-Request by a user";
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
