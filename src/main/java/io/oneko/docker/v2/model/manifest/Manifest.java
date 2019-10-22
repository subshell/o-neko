package io.oneko.docker.v2.model.manifest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Manifest {

	private String name;
	private String tag;
	private String architecture;
	private List<Layer> fsLayers;
	private List<Signature> signatures;
	private List<History> history;

	@JsonIgnore
	private String dockerContentDigest;

	@JsonIgnore
	public Optional<Instant> getImageUpdatedDate() {
		if (history != null && !history.isEmpty()) {
			return Optional.ofNullable(history.get(0).getV1Compatibility().getCreated());
		} else {
			return Optional.empty();
		}
	}
}
