package io.oneko.docker.v2.model.manifest;

import java.time.Instant;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Manifest {

	private String dockerContentDigest;
	private Instant imageUpdatedDate;

	@JsonIgnore
	public Optional<Instant> getImageUpdatedDate() {
		return Optional.ofNullable(imageUpdatedDate);
	}
}
