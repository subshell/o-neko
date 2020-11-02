package io.oneko.activity;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLog {

	List<Activity> getAll();

	List<Activity> getAllSince(LocalDateTime refDate);

	List<Activity> getAllPaged(int pageIndex, int pageSize);

}
