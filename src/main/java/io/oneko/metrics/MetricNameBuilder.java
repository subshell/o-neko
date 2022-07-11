package io.oneko.metrics;

/**
 * Helper class for naming metrics in compliance with
 * https://prometheus.io/docs/practices/naming/
 */
public class MetricNameBuilder {

    public static final String SEPARATOR = ".";
    public static final String DOMAIN = "oneko";

    //units and similar, like size, total, seconds etc.
    public static final String TOTAL = "total";
    public static final String DURATION = "duration";

    private final StringBuilder delegate;

    public MetricNameBuilder() {
        this.delegate = new StringBuilder(DOMAIN);
    }

    public MetricNameBuilder amountOf(String sizedThing) {
        delegate.append(SEPARATOR).append(sizedThing).append(SEPARATOR).append(TOTAL);
        return this;
    }

    public MetricNameBuilder durationOf(String measuredThing) {
        delegate.append(SEPARATOR).append(measuredThing).append(SEPARATOR).append(DURATION);
        return this;
    }

    public MetricNameBuilder with(String part) {
        delegate.append(SEPARATOR).append(part);
        return this;
    }

    public String build() {
        return delegate.toString();
    }

    @Override
    public String toString() {
        return build();
    }
}
