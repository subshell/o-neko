package io.oneko.templates;

import java.util.UUID;

import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareProperty;
import lombok.Builder;
import lombok.NonNull;

public class ConfigurationTemplate extends ModificationAwareIdentifiable {

	@NonNull
	private ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
	/**
	 * configuration template to be used for versions. Should be a multi line yaml-string.
	 */
	private ModificationAwareProperty<String> content = new ModificationAwareProperty<>(this, "content");
	private ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private ModificationAwareProperty<String> description = new ModificationAwareProperty<>(this, "description");

	public ConfigurationTemplate() {
		this.id.set(UUID.randomUUID());
	}

	@Builder
	public ConfigurationTemplate(UUID id, String content, String name, String description) {
		this.id.init(id);
		this.content.init(content);
		this.name.init(name);
		this.description.init(description);
	}

	public static ConfigurationTemplate clone(ConfigurationTemplate from) {
		return ConfigurationTemplate.builder()
				.name(from.getName())
				.id(from.getId())
				.description(from.getDescription())
				.content(from.getContent())
				.build();
	}

	public UUID getId() {
		return this.id.get();
	}

	public String getContent() {
		return content.get();
	}

	public void setContent(String content) {
		this.content.set(content);
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public String getDescription() {
		return description.get();
	}

	public void setDescription(String description) {
		this.description.set(description);
	}
}
