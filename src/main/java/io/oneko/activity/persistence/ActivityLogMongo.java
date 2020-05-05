package io.oneko.activity.persistence;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import io.oneko.activity.Activity;
import io.oneko.activity.internal.WritableActivityLog;
import io.oneko.domain.DescribingEntityChange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ActivityLogMongo implements WritableActivityLog {

	private final ActivityMongoSpringRepository springRepository;
	private final Sort sortByDateDesc = Sort.by(Sort.Direction.DESC, "date");

	@Autowired
	public ActivityLogMongo(ActivityMongoSpringRepository springRepository) {
		this.springRepository = springRepository;
	}

	@Override
	public Flux<Activity> getAll() {
		return springRepository.findAll(sortByDateDesc)
				.map(this::toActivity);
	}

	@Override
	public Flux<Activity> getAllSince(LocalDateTime refDate) {
		return springRepository.findByDateAfter(refDate, this.sortByDateDesc)
				.map(this::toActivity);
	}

	@Override
	public Flux<Activity> getAllPaged(int pageIndex, int pageSize) {
		return springRepository.findAll(sortByDateDesc)
				.skip(pageSize * pageIndex)
				.take(pageSize)
				.map(this::toActivity);
	}

	@Override
	public Mono<Activity> addActivity(Activity activity) {
		ActivityMongo activityMongo = toMongo(activity);
		return this.springRepository.insert(activityMongo)
				.map(this::toActivity);
	}

	@Override
	public Flux<Activity> deleteAllOlderThan(LocalDateTime date) {
		return springRepository.deleteByDateBefore(date)
				.map(this::toActivity);
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
