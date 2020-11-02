package io.oneko.activity.internal;

import java.time.LocalDateTime;

import io.oneko.activity.Activity;
import io.oneko.activity.ActivityLog;

public interface WritableActivityLog extends ActivityLog {

	Activity addActivity(Activity activity);

	void deleteAllOlderThan(LocalDateTime date);
}
