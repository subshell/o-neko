package io.oneko.templates;

import java.util.UUID;

import io.oneko.domain.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Builder
@Getter
public class ReadableConfigurationTemplate extends Identifiable implements ConfigurationTemplate {

	@NonNull
	private final UUID id;
	private final String content;
	private final String name;
	private final String description;
	private final String chartName;
	private final String chartVersion;
	private final UUID helmRegistryId;

	public WritableConfigurationTemplate writable() {
		return new WritableConfigurationTemplate(getId(), getContent(), getName(), getDescription(), chartName, chartVersion, helmRegistryId);
	}
}
