package io.oneko.event;

import io.oneko.project.ProjectVersion;

public class DeploymentRollbackEvent extends Event {

	private final ProjectVersion<?, ?> version;
	private final String message;

	public DeploymentRollbackEvent(ProjectVersion<?, ?> version, String message) {
		super();
		this.version = version;
		this.message = message;
	}

	@Override
	public String name() {
		return "Rollback project version " + version.getName() + "(" + version.getId() + ")";
	}

	@Override
	public String description() {
		return message;
	}
}
