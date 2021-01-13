package io.oneko.docker.event;

import io.oneko.event.Event;
import io.oneko.project.ProjectVersion;

import java.util.UUID;

public class NewProjectVersionFoundEvent extends Event {

	private final ProjectVersion version;

	public NewProjectVersionFoundEvent(ProjectVersion version) {
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
