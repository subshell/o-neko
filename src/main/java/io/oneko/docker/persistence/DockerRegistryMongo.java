package io.oneko.docker.persistence;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
class DockerRegistryMongo {
	@Id
	private UUID registryUuid;
	@Indexed(unique = true)
	private String name;
	private String registryUrl;
	private String userName;
	private String password;
	private boolean trustInsecureCertificate;
}
