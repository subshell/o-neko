package io.oneko.activity.persistence;

import com.google.common.collect.ImmutableList;
import io.oneko.Profiles;
import io.oneko.activity.Activity;
import io.oneko.activity.internal.WritableActivityLog;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
        final ArrayList<Activity> sortable = new ArrayList<>(new ArrayList<>(activities));
        sortable.sort(Comparator.comparing(Activity::getDate).reversed());
        return ImmutableList.copyOf(sortable);
    }

    @Override
    public List<Activity> getAllSince(LocalDateTime refDate) {
        return activities.stream()
                .filter(activity -> activity.getDate().isAfter(refDate))
                .sorted(Comparator.comparing(Activity::getDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Page<Activity> findAll(Pageable pageable) {
        List<Activity> activitiesInPage =  activities.stream()
                .sorted(Comparator.comparing(Activity::getDate).reversed())
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());

        return new PageImpl<>(activitiesInPage, pageable, activities.size());
    }
}
