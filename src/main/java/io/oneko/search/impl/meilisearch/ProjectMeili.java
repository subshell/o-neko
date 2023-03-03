package io.oneko.search.impl.meilisearch;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMeili {

	private UUID id;
	private String name;
	private String containerImage;
	private List<String> versionNames;

}
