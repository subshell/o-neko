package io.oneko.project.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class TemplateVariableMongo {
	@Id
	private UUID id;
	private String name;
	private String label;
	private List<String> values;
	private boolean useValues;
	private String defaultValue;
	private boolean showOnDashboard;
}
