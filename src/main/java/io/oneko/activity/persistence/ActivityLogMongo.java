package io.oneko.activity.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import io.oneko.Profiles;
import io.oneko.activity.Activity;
import io.oneko.activity.internal.WritableActivityLog;
import io.oneko.domain.DescribingEntityChange;

@Service
@Profile(Profiles.MONGO)
public class ActivityLogMongo implements WritableActivityLog {

	private final ActivityMongoSpringRepository springRepository;
	private final Sort sortByDateDesc = Sort.by(Sort.Direction.DESC, "date");

	@Autowired
	public ActivityLogMongo(ActivityMongoSpringRepository springRepository) {
		this.springRepository = springRepository;
	}

	@Override
	public List<Activity> getAll() {
		return springRepository.findAll(sortByDateDesc)
				.stream()
				.map(this::toActivity)
				.collect(Collectors.toList());
	}

	@Override
	public List<Activity> getAllSince(LocalDateTime refDate) {
		return springRepository.findByDateAfter(refDate, this.sortByDateDesc)
				.stream()
				.map(this::toActivity)
				.collect(Collectors.toList());
	}

	@Override
	public Page<Activity> findAll(Pageable pageable) {
		return springRepository.findAll(pageable).map(this::toActivity);
	}

	@Override
	public Activity addActivity(Activity activity) {
		ActivityMongo activityMongo = toMongo(activity);
		return toActivity(springRepository.insert(activityMongo));
	}

	@Override
	public void deleteAllOlderThan(LocalDateTime date) {
		springRepository.deleteByDateBefore(date);
	}

	private ActivityMongo toMongo(Activity activity) {
		final ActivityMongo.ActivityMongoBuilder activityMongoBuilder = ActivityMongo.builder()
				.id(activity.getId())
				.date(activity.getDate())
				.priority(activity.getPriority())
				// ActivityMongo uses 'name' for backwards compatability
				.name(activity.getTitle())
				.description(activity.getDescription())
				.activityType(activity.getActivityType())
				.typeOfTrigger(activity.getTriggerType())
				.nameOfTrigger(activity.getTriggerName());
		activity.getChangedEntity().ifPresent(describingEntityChange ->
				activityMongoBuilder.entityId(describingEntityChange.getId())
						.entityName(describingEntityChange.getName())
						.entityType(describingEntityChange.getEntityType())
						.changeType(describingEntityChange.getChangeType())
						.changedProperties(describingEntityChange.getChangedProperties())
		);
		return activityMongoBuilder.build();
	}

	private Activity toActivity(ActivityMongo activityMongo) {
		final Activity.ActivityBuilder activityBuilder = Activity.builder()
				.id(activityMongo.getId())
				.date(activityMongo.getDate())
				.priority(activityMongo.getPriority())

				// for backwards compatability - previously only description was set¡
				.title(StringUtils.isBlank(activityMongo.getName()) ? activityMongo.getDescription() : activityMongo.getName())
				.description(StringUtils.isBlank(activityMongo.getName()) ? "" : activityMongo.getDescription())

				.activityType(activityMongo.getActivityType())
				.triggerType(activityMongo.getTypeOfTrigger())
				.triggerName(activityMongo.getNameOfTrigger());
		if (activityMongo.getEntityId() != null) {
			activityBuilder.changedEntity(DescribingEntityChange.builder()
					.id(activityMongo.getEntityId())
					.name(activityMongo.getEntityName())
					.entityType(activityMongo.getEntityType())
					.changeType(activityMongo.getChangeType())
					.build());
		}

		return activityBuilder.build();
	}

}
