package io.oneko.docker.v2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {
	private String token;
	private String access_token;
	private int expires_in;
	private String issued_at;
}
