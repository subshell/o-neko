package io.oneko.docker.event;

import java.util.UUID;

import io.oneko.event.Event;
import io.oneko.event.EventTrigger;
import io.oneko.project.ProjectVersion;

public class NewProjectVersionFoundEvent extends Event {

	private final ProjectVersion version;

	public NewProjectVersionFoundEvent(ProjectVersion version, EventTrigger trigger) {
		super(trigger);
		this.version = version;
	}

	public UUID getVersionId() {
		return this.version.getId();
	}

	public UUID getProjectId() {
		return this.version.getProject().getId();
	}

	@Override
	public String humanReadable() {
		return "The new version " + version.getName() + " has been found for project " + version.getProject().getName() + ".";
	}
}
