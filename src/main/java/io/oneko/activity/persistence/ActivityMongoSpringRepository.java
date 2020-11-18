package io.oneko.activity.persistence;

import io.oneko.Profiles;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Profile(Profiles.MONGO)
public interface ActivityMongoSpringRepository extends MongoRepository<ActivityMongo, UUID> {
	List<ActivityMongo> findByDateAfter(LocalDateTime date, Sort sort);

	List<ActivityMongo> deleteByDateBefore(LocalDateTime date);

}
