package io.oneko.user.persistence;

import java.time.Instant;
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
public class ApiTokenMongo {

	@Id
	private UUID id;

	@Indexed
	private UUID userId;

	private String name;

	@Indexed(unique = true)
	private String tokenHash;

	private Instant createdAt;
	private Instant expiresAt;
	private Instant lastUsedAt;
}
