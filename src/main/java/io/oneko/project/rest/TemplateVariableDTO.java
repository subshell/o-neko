package io.oneko.project.rest;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateVariableDTO {
	private UUID id;
	private String name;
	private String label;
	private List<String> values;
	private boolean useValues;
	private String defaultValue;
	private boolean showOnDashboard;
}
