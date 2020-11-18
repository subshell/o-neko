package io.oneko.templates;

import io.oneko.domain.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

@AllArgsConstructor
@Builder
@Getter
public class ReadableConfigurationTemplate extends Identifiable implements ConfigurationTemplate {

	@NonNull
	private UUID id;
	private String content;
	private String name;
	private String description;

	public WritableConfigurationTemplate writable() {
		return new WritableConfigurationTemplate(getId(), getContent(), getName(), getDescription());
	}

}
