package io.oneko.configuration;

import java.time.Clock;

import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

	@Bean
	public ThreadPoolTaskScheduler taskScheduler(TaskSchedulerBuilder builder) {
		return builder.build();
	}

	@Bean()
	public Clock clock() {
		return Clock.systemUTC();
	}

}
