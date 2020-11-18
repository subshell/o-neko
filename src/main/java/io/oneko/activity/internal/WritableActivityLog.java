package io.oneko.activity.internal;

import io.oneko.activity.Activity;
import io.oneko.activity.ActivityLog;

import java.time.LocalDateTime;

public interface WritableActivityLog extends ActivityLog {

	Activity addActivity(Activity activity);

	void deleteAllOlderThan(LocalDateTime date);
}
