package io.oneko.activity.rest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.oneko.activity.ActivityLog;
import io.oneko.configuration.Controllers;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping(ActivityController.PATH)
public class ActivityController {

	public static final String PATH = Controllers.ROOT_PATH + "/activity";

	private final ActivityLog activityLog;
	private final ActivityDTOFactory activityDTOFactory;

	public ActivityController(ActivityLog activityLog, ActivityDTOFactory activityDTOFactory) {
		this.activityLog = activityLog;
		this.activityDTOFactory = activityDTOFactory;
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'DOER', 'VIEWER')")
	@GetMapping
	Page<ActivityDTO> getActivities(Pageable pageable) {
		return this.activityLog.findAll(pageable).map(this.activityDTOFactory::create);
	}

}
