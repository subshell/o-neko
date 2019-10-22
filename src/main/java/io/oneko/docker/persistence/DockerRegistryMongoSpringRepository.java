package io.oneko.docker.persistence;

import java.util.UUID;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Mono;

// https://github.com/hantsy/spring-reactive-sample/tree/master/security-data-mongo/src/main/java/com/example/demo
interface DockerRegistryMongoSpringRepository extends ReactiveMongoRepository<DockerRegistryMongo, UUID> {
	Mono<DockerRegistryMongo> findByName(String name);
}
