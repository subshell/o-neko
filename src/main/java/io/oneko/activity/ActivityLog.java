package io.oneko.activity;

import java.time.LocalDateTime;

import reactor.core.publisher.Flux;

public interface ActivityLog {

	Flux<Activity> getAll();

	Flux<Activity> getAllSince(LocalDateTime refDate);

	Flux<Activity> getAllPaged(int pageIndex, int pageSize);

}
