package io.oneko.helm;

import java.util.UUID;

public interface HelmRegistry {
	UUID getId();

	String getName();

	String getUrl();

	String getUsername();

	String getPassword();
}
