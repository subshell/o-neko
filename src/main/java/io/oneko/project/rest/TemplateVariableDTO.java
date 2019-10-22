package io.oneko.project.rest;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TemplateVariableDTO {
	private UUID id;
	private String name;
	private String label;
	private List<String> values;
	private boolean useValues;
	private String defaultValue;
	private boolean showOnDashboard;
}
