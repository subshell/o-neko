package io.oneko.docker.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DockerRegistryAPICheckDTO {
	private boolean okay;
	private String message;

	public static DockerRegistryAPICheckDTO okay(String message) {
		return new DockerRegistryAPICheckDTO(true, message);
	}

	public static DockerRegistryAPICheckDTO error(String errorMessage) {
		return new DockerRegistryAPICheckDTO(false, errorMessage);
	}
}
