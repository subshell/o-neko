package io.oneko.domain;

import java.util.Collection;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class DescribingEntityChange {

	private final EntityType entityType;
	private final ChangeType changeType;
	private final UUID id;
	private final String name;
	private final Collection<String> changedProperties;

	public enum EntityType {
		Project, DockerRegistry, User, Namespace, HelmRegistry
	}

	public enum ChangeType {
		Saved, Deleted
	}

}
