package io.oneko.activity.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import io.oneko.activity.Activity;
import io.oneko.activity.internal.WritableActivityLog;
import io.oneko.domain.DescribingEntityChange;

@Service
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
	public List<Activity> getAllPaged(int pageIndex, int pageSize) {
		return springRepository.findAll(PageRequest.of(pageIndex, pageSize, sortByDateDesc))
				.stream()
				.map(this::toActivity)
				.collect(Collectors.toList());
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
				.description(activityMongo.getDescription())
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
