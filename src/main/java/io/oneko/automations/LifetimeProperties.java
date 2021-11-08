package io.oneko.automations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@ConfigurationProperties("o-neko.deployments.lifetime")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LifetimeProperties {

	@Data
	@AllArgsConstructor
	public static class EndOfDay {
		private final Integer hour;
		private final Integer minute;
		private final Integer dayOffset;
	}

	private EndOfDay endOfDay;
	private String lastDayOfTheWeek;
}
