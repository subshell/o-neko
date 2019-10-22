package io.oneko.docker.event;

import java.util.UUID;

import io.oneko.event.Event;
import io.oneko.event.EventTrigger;
import io.oneko.project.ProjectVersion;
import lombok.Getter;

public class ObsoleteProjectVersionRemovedEvent extends Event {

	@Getter
	private final ProjectVersion version;

	public ObsoleteProjectVersionRemovedEvent(ProjectVersion version, EventTrigger trigger) {
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
		return "The version " + version.getName() + " of project " + version.getProject().getName() + " no longer exists in the docker registry and therefore has been removed.";
	}
}
