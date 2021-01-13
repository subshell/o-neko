package io.oneko.docker.v2;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;

import java.util.Base64;

@UtilityClass
public class AuthorizationHeader {

	public static final String KEY = HttpHeaders.AUTHORIZATION;

	public static String basic(String userName, String password) {
		return "Basic " + Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());
	}

	public static String bearer(String token) {
		return "Bearer " + token;
	}
}
