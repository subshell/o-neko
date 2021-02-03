package io.oneko.project.rest.export;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectExportMetadataDTO {
	private int version;
	private Instant exportedAt;
}
