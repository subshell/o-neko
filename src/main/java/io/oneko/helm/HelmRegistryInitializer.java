package io.oneko.helm;

import static io.oneko.util.MoreStructuredArguments.*;
import static net.logstash.logback.argument.StructuredArguments.*;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import io.oneko.helm.util.HelmCommandUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HelmRegistryInitializer {
	private final HelmRegistryRepository helmRegistryRepository;

	public HelmRegistryInitializer(HelmRegistryRepository helmRegistryRepository) {
		this.helmRegistryRepository = helmRegistryRepository;
	}

	@PostConstruct
	public void startup() {
		helmRegistryRepository.getAll().forEach(registry -> {
			try {
				HelmCommandUtils.addRegistry(registry);
				log.info("helm registry successfully added ({})", helmRegistryKv(registry));
			} catch (HelmRegistryException e) {
				log.error("error while adding helm registry during initialization ({})", helmRegistryKv(registry), e);
			}
		});
	}

}
