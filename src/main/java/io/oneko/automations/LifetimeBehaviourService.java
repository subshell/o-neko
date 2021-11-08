package io.oneko.automations;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import io.oneko.kubernetes.deployments.Deployment;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class LifetimeBehaviourService {
	public static final Map<String, Integer> DAY_OF_THE_WEEK = Map.ofEntries(
			Map.entry("SUNDAY", Calendar.SUNDAY),
			Map.entry("MONDAY", Calendar.MONDAY),
			Map.entry("TUESDAY", Calendar.TUESDAY),
			Map.entry("WEDNESDAY", Calendar.WEDNESDAY),
			Map.entry("THURSDAY", Calendar.THURSDAY),
			Map.entry("FRIDAY", Calendar.FRIDAY),
			Map.entry("SATURDAY", Calendar.SATURDAY)
	);

	private final LifetimeProperties lifetimeProperties;
	private final Clock clock;

	@PostConstruct
	void checkDayOfTheWeek() {
		if (!DAY_OF_THE_WEEK.containsKey(lifetimeProperties.getLastDayOfTheWeek().toUpperCase())) {
			throw new IllegalArgumentException("Lifetime configuration invalid. Last day of the week is unknown: "
					+ lifetimeProperties.getLastDayOfTheWeek()
					+ ". Possible values are " + StringUtils.join(DAY_OF_THE_WEEK.keySet()));
		}
	}

	public boolean isExpired(LifetimeBehaviour lifetimeBehaviour, Instant timestamp) {
		if (timestamp == null) {
			return lifetimeBehaviour.isInfinite();
		}

		if (lifetimeBehaviour.isInfinite()) {
			return false;
		}

		Instant expirationDate;
		if (lifetimeBehaviour.getType() == LifetimeBehaviourType.DAYS) {
			expirationDate = timestamp.plus(lifetimeBehaviour.getValue(), ChronoUnit.DAYS);
		} else if (lifetimeBehaviour.isUntilTonight()) {
			expirationDate = getCalendarAtEndOfDay(timestamp).toInstant();
		} else if (lifetimeBehaviour.isUntilWeekend()) {
			expirationDate = getCalendarAtNextWeekend(timestamp).toInstant();
		} else {
			throw new UnsupportedOperationException("lifetime behaviour type" + lifetimeBehaviour.getType() + " is not supported");
		}

		return clock.instant().isAfter(expirationDate);
	}

	public boolean isExpired(LifetimeBehaviour lifetimeBehaviour, Deployment deployment) {
		return isExpired(lifetimeBehaviour, deployment.getTimestamp().orElse(null));
	}

	private Calendar getCalendarAtEndOfDay(Instant timestamp) {
		LifetimeProperties.EndOfDay endOfDay = lifetimeProperties.getEndOfDay();

		Calendar date = GregorianCalendar.from(ZonedDateTime.ofInstant(timestamp, ZoneId.systemDefault()));
		date.add(Calendar.DATE, endOfDay.getDayOffset());
		date.set(Calendar.HOUR_OF_DAY, endOfDay.getHour());
		date.set(Calendar.MINUTE, endOfDay.getMinute());
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		return date;
	}

	private Calendar getCalendarAtNextWeekend(Instant timestamp) {
		Calendar date = getCalendarAtEndOfDay(timestamp);

		// next friday
		int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
		int daysUntilNextWeekOfDay = DAY_OF_THE_WEEK.get(lifetimeProperties.getLastDayOfTheWeek().toUpperCase()) - dayOfWeek;
		if (daysUntilNextWeekOfDay == 0) {
			daysUntilNextWeekOfDay = 7;
		}
		date.add(Calendar.DAY_OF_WEEK, daysUntilNextWeekOfDay);
		return date;
	}
}
