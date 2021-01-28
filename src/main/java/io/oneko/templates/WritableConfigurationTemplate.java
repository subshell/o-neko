package io.oneko.templates;

import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareProperty;
import lombok.Builder;
import lombok.NonNull;

import java.util.UUID;

public class WritableConfigurationTemplate extends ModificationAwareIdentifiable implements ConfigurationTemplate {

	@NonNull
	private ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
	/**
	 * configuration template to be used for versions. Should be a multi line yaml-string.
	 */
	private ModificationAwareProperty<String> content = new ModificationAwareProperty<>(this, "content");
	private ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private ModificationAwareProperty<String> description = new ModificationAwareProperty<>(this, "description");
	private ModificationAwareProperty<String> chartName = new ModificationAwareProperty<>(this, "chartName");
	private ModificationAwareProperty<String> chartVersion = new ModificationAwareProperty<>(this, "chartVersion");
	private ModificationAwareProperty<UUID> helmRegistryId = new ModificationAwareProperty<>(this, "helmRegistryId");

	public WritableConfigurationTemplate() {
		this.id.set(UUID.randomUUID());
	}

	@Builder
	public WritableConfigurationTemplate(UUID id, String content, String name, String description, String chartName, String chartVersion, UUID helmRegistryId) {
		this.id.init(id);
		this.content.init(content);
		this.name.init(name);
		this.description.init(description);
		this.chartName.init(chartName);
		this.chartVersion.init(chartVersion);
		this.helmRegistryId.init(helmRegistryId);
	}

	public static WritableConfigurationTemplate clone(ConfigurationTemplate from) {
		return WritableConfigurationTemplate.builder()
				.name(from.getName())
				.id(from.getId())
				.description(from.getDescription())
				.content(from.getContent())
				.chartName(from.getChartName())
				.chartVersion(from.getChartVersion())
				.helmRegistryId(from.getHelmRegistryId())
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

	@Override
	public String getChartName() {
		return this.chartName.get();
	}

	public void setChartName(String name) {
		this.chartName.set(name);
	}

	@Override
	public String getChartVersion() {
		return this.chartVersion.get();
	}

	public void setChartVersion(String chartVersion) {
		this.chartVersion.set(chartVersion);
	}

	@Override
	public UUID getHelmRegistryId() {
		return this.helmRegistryId.get();
	}

	public void setHelmRegistryId(UUID helmRegistryId) {
		this.helmRegistryId.set(helmRegistryId);
	}

	public void setDescription(String description) {
		this.description.set(description);
	}

	public ReadableConfigurationTemplate readable() {
		return new ReadableConfigurationTemplate(getId(), getContent(), getName(), getDescription(), getChartName(), getChartVersion(), getHelmRegistryId());
	}
}
