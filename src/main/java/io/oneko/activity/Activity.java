package io.oneko.activity;

import io.oneko.domain.DescribingEntityChange;
import io.oneko.domain.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Activities are views on events that happened earlier.
 */
@AllArgsConstructor
@Builder
public class Activity extends Identifiable {

	@Getter
	private final UUID id;
	@Getter
	private final LocalDateTime date;
	@Getter
	private final ActivityPriority priority;
	@Getter
	private final String name;
	@Getter
	private final String description;
	/**
	 * This typically corresponds the name of an event that caused the activity in the first place
	 */
	@Getter
	private final String activityType;

	@Getter
	private final String triggerName;
	@Getter
	private final String triggerType;

	private final DescribingEntityChange changedEntity;

	/**
	 * Some activities might refer to a single entity. In this case, this holds certain meta information from the moment
	 * the activity happened.
	 */
	public Optional<DescribingEntityChange> getChangedEntity() {
		return Optional.ofNullable(changedEntity);
	}
}
