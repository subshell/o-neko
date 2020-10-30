package io.oneko.user.persistence;

import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Wins the award for the longest interface name... For a short name use the acronym "PBUAMSR"
 */
public interface PasswordBasedUserAuthenticationMongoSpringRepository extends MongoRepository<PasswordBasedUserAuthenticationMongo, UUID> {
}
