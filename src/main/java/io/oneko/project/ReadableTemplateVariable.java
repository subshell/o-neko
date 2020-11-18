package io.oneko.project;

import com.google.common.collect.ImmutableList;
import io.oneko.domain.Identifiable;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class ReadableTemplateVariable extends Identifiable implements TemplateVariable {
	private final UUID id;
	private final String name;
	private final String label;
	private final ImmutableList<String> values;
	private final boolean useValues;
	private final String defaultValue;
	private final boolean showOnDashboard;

	@Builder
	public ReadableTemplateVariable(UUID id, String name, String label, List<String> values, Boolean useValues, String defaultValue, Boolean showOnDashboard) {
		this.id = id;
		this.name = name;
		this.label = label;
		this.values = ImmutableList.copyOf(values);
		this.useValues = useValues;
		this.defaultValue = defaultValue;
		this.showOnDashboard = showOnDashboard;
	}

	public WritableTemplateVariable writable() {
		return WritableTemplateVariable.builder()
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
