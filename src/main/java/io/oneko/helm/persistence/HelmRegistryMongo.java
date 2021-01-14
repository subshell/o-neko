package io.oneko.helm.persistence;

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
class HelmRegistryMongo {
	@Id
	private UUID id;
	@Indexed(unique = true)
	private String name;
	private String url;
	private String username;
	private String password;
}
