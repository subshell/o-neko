package io.oneko.activity.rest;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import io.oneko.activity.ActivityPriority;
import io.oneko.domain.DescribingEntityChange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ActivityDTO {
	private UUID id;
	private LocalDateTime date;
	private ActivityPriority priority;
	private String description;
	private String activityType;

	private String triggerName;
	private String triggerType;

	private UUID entityId;
	private String entityName;
	private DescribingEntityChange.EntityType entityType;
	private DescribingEntityChange.ChangeType changeType;
	private Collection<String> changedEntityProperties;
}
