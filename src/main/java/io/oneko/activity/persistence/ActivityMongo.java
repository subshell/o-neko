package io.oneko.activity.persistence;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import io.oneko.activity.ActivityPriority;
import io.oneko.domain.DescribingEntityChange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class ActivityMongo {
	/*--------------------------------
	 *general stuff
	 --------------------------------*/
	@Id
	private UUID id;
	private LocalDateTime date;
	private String activityType;
	private String description;
	private ActivityPriority priority;

    /*--------------------------------
     *related to the trigger
     --------------------------------*/

	@Field("trigger")
	private String nameOfTrigger;
	@Field("typeId")
	private String typeOfTrigger;

	/*--------------------------------
	 * stuff related to the entity (all optional)
	 --------------------------------*/
	private Collection<String> changedProperties;
	private UUID entityId;
	private String entityName;
	private DescribingEntityChange.EntityType entityType;
	private DescribingEntityChange.ChangeType changeType;
}
