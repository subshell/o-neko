package io.oneko.activity;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActivityLog {

	List<Activity> getAll();

	List<Activity> getAllSince(LocalDateTime refDate);

	Page<Activity> findAll(Pageable pageable);

}
