package io.oneko.project;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.oneko.domain.ModificationAwareIdentifiable;
import io.oneko.domain.ModificationAwareListProperty;
import io.oneko.domain.ModificationAwareProperty;

public class TemplateVariable extends ModificationAwareIdentifiable {
	private final ModificationAwareProperty<UUID> uuid = new ModificationAwareProperty<>(this, "uuid");
	private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");
	private final ModificationAwareProperty<String> label = new ModificationAwareProperty<>(this, "label");
	private final ModificationAwareListProperty<String> values = new ModificationAwareListProperty<>(this, "values");
	private final ModificationAwareProperty<Boolean> useValues = new ModificationAwareProperty<>(this, "useValues");
	private final ModificationAwareProperty<String> defaultValue = new ModificationAwareProperty<>(this, "defaultValue");
	private final ModificationAwareProperty<Boolean> showOnDashboard = new ModificationAwareProperty<>(this, "showOnDashboard");

	public TemplateVariable(String name) {
		this(name, name, new ArrayList<>(), false, null, false);
	}

	public TemplateVariable(String name, String label, List<String> values, boolean useValues, String defaultValue, boolean showOnDashboard) {
		this(UUID.randomUUID(), name, label, values, useValues, defaultValue, showOnDashboard);
	}

	public TemplateVariable(UUID uuid, String name, String label, List<String> values, boolean useValues, String defaultValue, boolean showOnDashboard) {
		this.uuid.init(uuid);
		this.name.init(name);
		this.label.init(label);
		this.values.init(values);
		this.useValues.init(useValues);
		this.defaultValue.init(defaultValue);
		this.showOnDashboard.init(showOnDashboard);
	}

	@Override
	public UUID getId() {
		return this.uuid.get();
	}

	public void setId(UUID id) {
		this.uuid.set(id);
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
}
