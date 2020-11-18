package io.oneko.user.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * MongoDB view on a User
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class UserMongo {
	@Id
	private UUID userUuid;
	@Indexed(unique = true)
	private String username;
	@Indexed(unique = true)
	private String email;

	private String firstName;
	private String lastName;
	private String role;

}
