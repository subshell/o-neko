package io.oneko.util;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.Setter;

/**
 * This bucket holds values with a time to live and expires them passively on access.
 */
public class ExpiringBucket<T> implements Iterable<T> {

	@Getter
	@Setter
	private Duration ttl;

	private Map<T, Instant> values = new HashMap<>();

	public ExpiringBucket(Duration ttl) {
		this.ttl = ttl;
	}

	public ExpiringBucket<T> concurrent() {
		if (!(values instanceof ConcurrentHashMap)) {
			this.values = new ConcurrentHashMap<>(this.values);
		}
		return this;
	}

	public int size() {
		purgeExpiredValues();
		return values.size();
	}

	public boolean isEmpty() {
		purgeExpiredValues();
		return values.isEmpty();
	}

	public boolean contains(T o) {
		checkAndInvalidateValue(o);
		return values.containsKey(o);
	}

	@Override
	public Iterator<T> iterator() {
		return values.keySet().iterator();
	}

	public void add(T t) {
		values.put(t, Instant.now());
	}

	public void remove(T o) {
		values.remove(o);
	}

	private void checkAndInvalidateValue(T o) {
		if (values.containsKey(o)) {
			final Instant date = values.get(o);
			if (date.plus(ttl).isBefore(Instant.now())) {
				values.remove(o);
			}
		}
	}

	public void purgeExpiredValues() {
		values.keySet().forEach(this::checkAndInvalidateValue);
	}

	public List<T> getAll() {
		return new ArrayList<>(values.keySet());
	}
}
