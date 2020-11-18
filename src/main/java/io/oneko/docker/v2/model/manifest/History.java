package io.oneko.docker.v2.model.manifest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class History {

	private static ObjectMapper helperMapper = new ObjectMapper();

	static {
		helperMapper.findAndRegisterModules();
	}

	private V1Compatibility v1Compatibility;

	@JsonSetter
	public void setV1Compatibility(String v1Compatibility) {
		try {
			this.v1Compatibility = helperMapper.readValue(v1Compatibility, V1Compatibility.class);
		} catch (IOException e) {
			log.debug("Failed to deserialize History/v1Compatibility from Docker Image Manifest.", e);
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class V1Compatibility {

		private Instant created;
		// there are more properties here but we're only interested in the date
	}

}
