package io.oneko.activity.internal;

import java.time.LocalDateTime;

import io.oneko.activity.Activity;
import io.oneko.activity.ActivityLog;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WritableActivityLog extends ActivityLog {

	Mono<Activity> addActivity(Activity activity);

	Flux<Activity> deleteAllOlderThan(LocalDateTime date);
}
