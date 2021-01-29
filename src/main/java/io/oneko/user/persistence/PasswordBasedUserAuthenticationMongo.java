package io.oneko.user.persistence;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MongoDB view on a PasswordBasedUserAuthentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class PasswordBasedUserAuthenticationMongo {
	@Id
	private UUID userUuid;
	private String password;

}
