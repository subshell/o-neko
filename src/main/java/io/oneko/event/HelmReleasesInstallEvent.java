package io.oneko.event;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.oneko.project.ProjectVersion;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HelmReleasesInstallEvent extends Event {

	private final ProjectVersion<?, ?> version;
	private final List<String> releaseNames;

	@Override
	public String title() {
		return "Install releases for project version " + version.getName();
	}

	@Override
	public String description() {
		return "Release names: " + StringUtils.join(releaseNames, ", ");
	}
}
