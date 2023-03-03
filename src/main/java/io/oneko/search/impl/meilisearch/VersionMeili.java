package io.oneko.search.impl.meilisearch;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionMeili {

	private UUID id;
	private String name;
	private UUID projectId;
}
