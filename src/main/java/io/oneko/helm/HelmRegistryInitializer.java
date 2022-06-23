package io.oneko.helm;

import static io.oneko.util.MoreStructuredArguments.*;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HelmRegistryInitializer {
	private final HelmRegistryRepository helmRegistryRepository;
	private final HelmCommands helmCommands;

	public HelmRegistryInitializer(HelmRegistryRepository helmRegistryRepository, HelmCommands helmCommands) {
		this.helmRegistryRepository = helmRegistryRepository;
		this.helmCommands = helmCommands;
	}

	@PostConstruct
	public void startup() {
		helmRegistryRepository.getAll().forEach(registry -> {
			try {
				helmCommands.addRegistry(registry);
				log.info("helm registry successfully added ({})", helmRegistryKv(registry));
			} catch (HelmRegistryException e) {
				log.error("error while adding helm registry during initialization ({})", helmRegistryKv(registry), e);
			}
		});
	}

}
