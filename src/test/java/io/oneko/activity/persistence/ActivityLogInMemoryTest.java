package io.oneko.activity.persistence;

import static org.assertj.core.api.Assertions.*;

import io.oneko.activity.Activity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

class ActivityLogInMemoryTest {

    ActivityLogInMemory uut = new ActivityLogInMemory();

    @Test
    void testCrud() {
        assertThat(uut.getAll()).isEmpty();

        Activity activity = Activity.builder().id(UUID.randomUUID()).activityType("project").date(LocalDateTime.now()).build();
        uut.addActivity(activity);

        assertThat(uut.getAll()).hasSize(1);
    }

    @Test
    void testSorting() {
        Activity activity1 = Activity.builder().id(UUID.randomUUID()).activityType("project").date(LocalDateTime.of(2021, 1, 1, 0 ,0, 0)).build();

        Activity activity2 = Activity.builder().id(UUID.randomUUID()).activityType("project").date(LocalDateTime.of(2021, 1, 1, 0 ,1, 0)).build();

        Activity activity3 = Activity.builder().id(UUID.randomUUID()).activityType("project").date(LocalDateTime.of(2021, 1, 1, 0 ,2, 0)).build();

        // add in "random" order
        uut.addActivity(activity1);
        uut.addActivity(activity3);
        uut.addActivity(activity2);

        final List<Activity> all = uut.getAll();

        assertThat(all).containsExactly(activity3, activity2, activity1);
    }

}
