package io.oneko.helmapi.model;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chart {
	private String name;
	private String version;
	@SerializedName("app_version")
	private String appVersion;
	private String description;
}
