package io.oneko.docker.rest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChangeDockerRegistryPasswordDTO {
	private String password;
}
