package io.oneko.helm;

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
				log.info("Helm registry {} successfully added.", registry.getName());
			} catch (HelmRegistryException e) {
				log.error("Error while adding helm registry {} during initialization.", registry.getName(), e);
			}
		});
	}

}
