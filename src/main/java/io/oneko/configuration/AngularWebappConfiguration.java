package io.oneko.configuration;

import java.io.IOException;
import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.resource.GzipResourceResolver;
import org.springframework.web.reactive.resource.PathResourceResolver;

import reactor.core.publisher.Mono;

@Configuration
public class AngularWebappConfiguration implements WebFluxConfigurer {

	@Bean
	public TaskScheduler taskScheduler() {
		return new ConcurrentTaskScheduler();
	}

	@Bean
	public Executor taskExecutor() {
		return new SimpleAsyncTaskExecutor();
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**")
				.addResourceLocations("classpath:/public/")
				.resourceChain(true)
				.addResolver(new GzipResourceResolver())
				.addResolver(new PathResourceResolver() {
					@Override
					protected Mono<Resource> getResource(String resourcePath,
														 Resource location) {
						Resource requestedResource;
						try {
							requestedResource = location.createRelative(resourcePath);
						} catch (IOException e) {
							return Mono.just(new ClassPathResource("/public/index.html"));
						}
						return requestedResource.exists() && requestedResource.isReadable() ? Mono.just(requestedResource)
								: Mono.just(new ClassPathResource("/public/index.html"));
					}
				});
	}
}
