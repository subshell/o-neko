package io.oneko.helm;

import io.oneko.helmapi.process.CommandException;

public class HelmRegistryException extends Exception {

	public HelmRegistryException(String msg) {
		super(msg);
	}

	public HelmRegistryException(String msg, Exception e) {
		super(msg, e);
	}

	public static HelmRegistryException fromCommandException(CommandException e, String url, String name) {
		return new HelmRegistryException(String.format("Error while adding helm registry %s (%s): %s", url, name, e.getMessage()), e);
	}

}
