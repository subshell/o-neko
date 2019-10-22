package io.oneko.activity.persistence;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;

public interface ActivityMongoSpringRepository extends ReactiveMongoRepository<ActivityMongo, UUID> {
	Flux<ActivityMongo> findByDateAfter(LocalDateTime date, Sort sort);

	Flux<ActivityMongo> deleteByDateBefore(LocalDateTime date);

}
