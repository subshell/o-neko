package io.oneko.user.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;

interface ApiTokenMongoSpringRepository extends MongoRepository<ApiTokenMongo, UUID> {

	Optional<ApiTokenMongo> findByTokenHash(String tokenHash);

	List<ApiTokenMongo> findByUserId(UUID userId);

	void deleteAllByUserId(UUID userId);
}
