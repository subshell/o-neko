package io.oneko.helmapi.model;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class Release {
	String name;
	String namespace;
	int revision;
	Date updated;
	ReleaseStatus status;
	String chart;
	@SerializedName("app_version")
	String appVersion;
}
