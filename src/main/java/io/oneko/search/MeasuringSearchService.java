package io.oneko.search;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.oneko.metrics.MetricNameBuilder;
import lombok.Builder;

public abstract class MeasuringSearchService implements SearchService {

	protected final MeterRegistry meterRegistry;
	protected final Timer queryDurationTimer;

	public MeasuringSearchService(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
		this.queryDurationTimer = Timer.builder(new MetricNameBuilder().durationOf("search.query")
						.build())
				.publishPercentileHistogram()
				.register(meterRegistry);
	}

}
