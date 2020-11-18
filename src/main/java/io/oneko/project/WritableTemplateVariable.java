package io.oneko.project;

import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareListProperty;
import io.oneko.domain.ModificationAwareProperty;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WritableTemplateVariable extends ModificationAwareIdentifiable implements TemplateVariable {
	private final ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
	private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private final ModificationAwareProperty<String> label = new ModificationAwareProperty<>(this, "label");
	private final ModificationAwareListProperty<String> values = new ModificationAwareListProperty<>(this, "values");
	private final ModificationAwareProperty<Boolean> useValues = new ModificationAwareProperty<>(this, "useValues");
	private final ModificationAwareProperty<String> defaultValue = new ModificationAwareProperty<>(this, "defaultValue");
	private final ModificationAwareProperty<Boolean> showOnDashboard = new ModificationAwareProperty<>(this, "showOnDashboard");

	public WritableTemplateVariable(String name) {
		this(name, name, new ArrayList<>(), false, null, false);
	}

	public WritableTemplateVariable(String name, String label, List<String> values, boolean useValues, String defaultValue, boolean showOnDashboard) {
		this(UUID.randomUUID(), name, label, values, useValues, defaultValue, showOnDashboard);
	}

	@Builder
	public WritableTemplateVariable(UUID id, String name, String label, List<String> values, boolean useValues, String defaultValue, boolean showOnDashboard) {
		this.id.init(id);
		this.name.init(name);
		this.label.init(label);
		this.values.init(values);
		this.useValues.init(useValues);
		this.defaultValue.init(defaultValue);
		this.showOnDashboard.init(showOnDashboard);
	}

	@Override
	public UUID getId() {
		return this.id.get();
	}

	//shouldn't we avoid setter for IDs?
	public void setId(UUID id) {
		this.id.set(id);
	}

	public String getName() {
		return this.name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public List<String> getValues() {
		return this.values.get();
	}

	public void setValues(List<String> values) {
		this.values.set(values);
	}

	public boolean isUseValues() {
		return this.useValues.get();
	}

	public void setUseValues(boolean useValues) {
		this.useValues.set(useValues);
	}

	public String getDefaultValue() {
		return this.defaultValue.get();
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue.set(defaultValue);
	}

	public String getLabel() {
		return this.label.get();
	}

	public void setLabel(String label) {
		this.label.set(label);
	}

	public boolean isShowOnDashboard() {
		return this.showOnDashboard.get();
	}

	public void setShowOnDashboard(boolean showOnDashboard) {
		this.showOnDashboard.set(showOnDashboard);
	}

	public ReadableTemplateVariable readable() {
		return ReadableTemplateVariable.builder()
				.id(getId())
				.name(getName())
				.label(getLabel())
				.values(getValues())
				.useValues(isUseValues())
				.defaultValue(getDefaultValue())
				.showOnDashboard(isShowOnDashboard())
				.build();
	}
}
