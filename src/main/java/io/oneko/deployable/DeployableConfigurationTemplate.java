package io.oneko.deployable;

import lombok.Getter;

@Getter
public class DeployableConfigurationTemplate {

	private final String content;
	private final String name;
	private final String contentHash;

	public DeployableConfigurationTemplate(String content, String name) {
		this.content = content;
		this.name = name;
		this.contentHash = String.valueOf(content.hashCode());
	}
}
