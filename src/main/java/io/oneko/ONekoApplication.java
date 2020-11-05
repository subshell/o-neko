package io.oneko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;

@SpringBootApplication
@EnableMongoHttpSession(maxInactiveIntervalInSeconds = 86400)
@EnableScheduling
@EnableMongoRepositories
public class ONekoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ONekoApplication.class, args);
	}
}
