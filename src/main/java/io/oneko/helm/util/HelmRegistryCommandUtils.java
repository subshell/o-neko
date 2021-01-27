package io.oneko.helm.util;

import io.oneko.helm.HelmRegistryException;
import io.oneko.helm.ReadableHelmRegistry;
import io.oneko.helmapi.api.Helm;
import io.oneko.helmapi.process.CommandException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class HelmRegistryCommandUtils {

	public static void addRegistry(ReadableHelmRegistry helmRegistry) throws HelmRegistryException {
		var helm = new Helm();

		try {
			helm.addRepo(helmRegistry.getName(), helmRegistry.getUrl(), helmRegistry.getUsername(), helmRegistry.getPassword());
		} catch (CommandException e) {
			throw HelmRegistryException.fromCommandException(e, helmRegistry.getUrl(), helmRegistry.getName());
		}
	}

	public static void deleteRegistry(ReadableHelmRegistry helmRegistry) throws HelmRegistryException {
		var helm = new Helm();

		try {
			helm.removeRepo(helmRegistry.getName());
		} catch (CommandException e) {
			throw HelmRegistryException.fromCommandException(e, helmRegistry.getUrl(), helmRegistry.getName());
		}
	}
}
