package io.oneko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.mongo.config.annotation.web.reactive.EnableMongoWebSession;

@SpringBootApplication
@EnableMongoWebSession(maxInactiveIntervalInSeconds = 86400)
@EnableScheduling
public class ONekoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ONekoApplication.class, args);
	}
}
