package io.oneko.configuration;

import io.oneko.Profiles;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(Profiles.MONGO)
public class MongoDataConfiguration extends MongoDataAutoConfiguration {
}
