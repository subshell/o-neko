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

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class AngularWebappConfiguration implements WebMvcConfigurer {

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
				.addResolver(new EncodedResourceResolver())
				.addResolver(new PathResourceResolver() {
					@Override
					protected Resource getResource(String resourcePath,
					                               Resource location) {
						Resource requestedResource;
						try {
							requestedResource = location.createRelative(resourcePath);
						} catch (IOException e) {
							return new ClassPathResource("/public/index.html");
						}
						return requestedResource.exists() && requestedResource.isReadable()
								? requestedResource
								: new ClassPathResource("/public/index.html");
					}
				});
	}

}
