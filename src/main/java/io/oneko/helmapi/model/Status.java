package io.oneko.helmapi.model;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Status {
	private String name;
	private Info info;
	private String manifest;
	private int version;
	private String namespace;

	@Data
	@NoArgsConstructor
	public static class Info {
		@SerializedName("first_deployed")
		private Date firstDeployed;
		@SerializedName("last_deployed")
		private Date lastDeployed;
		private boolean deleted;
		private String description;
		private ReleaseStatus status;
	}
}
