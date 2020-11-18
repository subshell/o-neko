package io.oneko.activity.persistence;

import com.google.common.collect.ImmutableList;
import io.oneko.Profiles;
import io.oneko.activity.Activity;
import io.oneko.activity.internal.WritableActivityLog;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile(Profiles.IN_MEMORY)
public class ActivityLogInMemory implements WritableActivityLog {

    List<Activity> activities = new ArrayList<>();

    @Override
    public Activity addActivity(Activity activity) {
        activities.add(activity);
        return activity;
    }

    @Override
    public void deleteAllOlderThan(LocalDateTime date) {
        this.activities.removeIf(activity -> activity.getDate().isBefore(date));
    }

    @Override
    public List<Activity> getAll() {
        return ImmutableList.copyOf(activities);
    }

    @Override
    public List<Activity> getAllSince(LocalDateTime refDate) {
        return activities.stream()
                .filter(activity -> activity.getDate().isAfter(refDate))
                .collect(Collectors.toList());
    }

    @Override
    public List<Activity> getPaged(int pageIndex, int pageSize) {
        return activities.stream()
                .skip(pageIndex * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }
}
