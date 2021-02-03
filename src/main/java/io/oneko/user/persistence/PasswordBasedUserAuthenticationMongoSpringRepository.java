package io.oneko.user.persistence;

import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import io.oneko.Profiles;

/**
 * Wins the award for the longest interface name... For a short name use the acronym "PBUAMSR"
 */
@Profile(Profiles.MONGO)
public interface PasswordBasedUserAuthenticationMongoSpringRepository extends MongoRepository<PasswordBasedUserAuthenticationMongo, UUID> {
}
