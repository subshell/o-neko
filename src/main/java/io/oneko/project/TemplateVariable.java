package io.oneko.project;

import java.util.List;
import java.util.UUID;

public interface TemplateVariable {

	UUID getId();

	String getName();

	List<String> getValues();

	boolean isUseValues();

	String getDefaultValue();

	String getLabel();

	boolean isShowOnDashboard();

}
