package io.oneko.activity;

public enum ActivityPriority {
	INFO("info"),
	WARNING("warning"),
	ERROR("error");

	private String key;

	ActivityPriority(String key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return this.key;
	}
}
