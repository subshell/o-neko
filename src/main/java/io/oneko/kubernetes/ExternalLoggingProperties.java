package io.oneko.kubernetes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("o-neko.deployments.logs")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ExternalLoggingProperties {
	private String externalLogUrlTemplate = "";
}
