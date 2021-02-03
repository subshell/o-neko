package io.oneko.helm.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import io.oneko.Profiles;

@Profile(Profiles.MONGO)
public interface HelmRegistryMongoSpringRepository extends MongoRepository<HelmRegistryMongo, UUID> {
	Optional<HelmRegistryMongo> findByName(String name);
}
