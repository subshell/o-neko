package io.oneko.user.persistence;

import io.oneko.Profiles;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

/**
 * Wins the award for the longest interface name... For a short name use the acronym "PBUAMSR"
 */
@Profile(Profiles.MONGO)
public interface PasswordBasedUserAuthenticationMongoSpringRepository extends MongoRepository<PasswordBasedUserAuthenticationMongo, UUID> {
}
