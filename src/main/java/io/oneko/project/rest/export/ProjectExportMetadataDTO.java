package io.oneko.project.rest.export;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ProjectExportMetadataDTO {
	private int version;
	private Instant exportedAt;
}
