package io.oneko.project.persistence;

import java.util.UUID;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// https://github.com/hantsy/spring-reactive-sample/tree/master/security-data-mongo/src/main/java/com/example/demo
interface ProjectMongoSpringRepository extends ReactiveMongoRepository<ProjectMongo, UUID> {
	Mono<ProjectMongo> findByName(String name);

	Flux<ProjectMongo> findByDockerRegistryUUID(UUID dockerRegistryUUID);
}
