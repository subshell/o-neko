package io.oneko.user.persistence;

import io.oneko.Profiles;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

// https://github.com/hantsy/spring-reactive-sample/tree/master/security-data-mongo/src/main/java/com/example/demo
@Profile(Profiles.MONGO)
public interface UserMongoSpringRepository extends MongoRepository<UserMongo, UUID> {
	Optional<UserMongo> findByUsername(String username);

	Optional<UserMongo> findByEmail(String email);
}
