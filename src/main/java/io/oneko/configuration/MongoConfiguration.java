package io.oneko.configuration;

import io.oneko.Profiles;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;

@Configuration
@Profile(Profiles.MONGO)
//@EnableMongoHttpSession(maxInactiveIntervalInSeconds = 86400)
//@EnableMongoRepositories
public class MongoConfiguration extends MongoAutoConfiguration {
}
