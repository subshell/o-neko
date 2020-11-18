package io.oneko.user.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

/**
 * Wins the award for the longest interface name... For a short name use the acronym "PBUAMSR"
 */
public interface PasswordBasedUserAuthenticationMongoSpringRepository extends MongoRepository<PasswordBasedUserAuthenticationMongo, UUID> {
}
