package io.oneko.user.persistence;

import java.util.UUID;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

// https://github.com/hantsy/spring-reactive-sample/tree/master/security-data-mongo/src/main/java/com/example/demo
public interface UserMongoSpringRepository extends ReactiveMongoRepository<UserMongo, UUID> {
	Mono<UserMongo> findByUsername(String username);

	Mono<UserMongo> findByEmail(String email);
}
