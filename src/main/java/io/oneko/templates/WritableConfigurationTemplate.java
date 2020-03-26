package io.oneko.templates;

import java.util.UUID;

import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareProperty;
import lombok.Builder;
import lombok.NonNull;

public class WritableConfigurationTemplate extends ModificationAwareIdentifiable implements ConfigurationTemplate {

	@NonNull
	private ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
	/**
	 * configuration template to be used for versions. Should be a multi line yaml-string.
	 */
	private ModificationAwareProperty<String> content = new ModificationAwareProperty<>(this, "content");
	private ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private ModificationAwareProperty<String> description = new ModificationAwareProperty<>(this, "description");

	public WritableConfigurationTemplate() {
		this.id.set(UUID.randomUUID());
	}

	@Builder
	public WritableConfigurationTemplate(UUID id, String content, String name, String description) {
		this.id.init(id);
		this.content.init(content);
		this.name.init(name);
		this.description.init(description);
	}

	public static WritableConfigurationTemplate clone(ConfigurationTemplate from) {
		return WritableConfigurationTemplate.builder()
				.name(from.getName())
				.id(from.getId())
				.description(from.getDescription())
				.content(from.getContent())
				.build();
	}

	@Override
	public UUID getId() {
		return this.id.get();
	}

	@Override
	public String getContent() {
		return content.get();
	}

	public void setContent(String content) {
		this.content.set(content);
	}

	@Override
	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	@Override
	public String getDescription() {
		return description.get();
	}

	public void setDescription(String description) {
		this.description.set(description);
	}

	public ReadableConfigurationTemplate readable() {
		return new ReadableConfigurationTemplate(getId(), getContent(), getName(), getDescription());
	}
}
