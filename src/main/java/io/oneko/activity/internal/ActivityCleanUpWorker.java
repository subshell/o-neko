package io.oneko.activity.internal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@ConditionalOnProperty(name = "o-neko.activity.cleanup.maxAgeHours")
public class ActivityCleanUpWorker {
	private final WritableActivityLog activityRepo;
	private final int maxActivityAgeHours;

	@Autowired
	public ActivityCleanUpWorker(
			WritableActivityLog activityRepo,
			@Value("${o-neko.activity.cleanup.maxAgeHours}") final int maxActivityAgeHours) {
		this.activityRepo = activityRepo;
		this.maxActivityAgeHours = maxActivityAgeHours;
	}

	@Scheduled(fixedDelayString = "${o-neko.activity.cleanup.schedulerIntervalMillis:3600000}")
	private void cleanUpActivities() {
		LocalDateTime expirationDate = LocalDateTime.now().minusHours(maxActivityAgeHours);
		log.info("Deleting activities that are older than {} hours.", maxActivityAgeHours);
		activityRepo.deleteAllOlderThan(expirationDate);
	}
}
