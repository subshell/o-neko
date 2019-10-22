package io.oneko.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModificationAwareMapProperty<T1, T2> extends ModificationAwareProperty<Map<T1, T2>> {

	public ModificationAwareMapProperty(ModificationAwareIdentifiable entity, String propertyName) {
		super(entity, propertyName);
		super.init(Collections.emptyMap());
	}

	@Override
	public void init(Map<T1, T2> value) {
		if (value == null) {
			super.init(Collections.emptyMap());
		} else {
			super.init(new HashMap<>(value));
		}
	}

	@Override
	public boolean set(Map<T1, T2> value) {
		if (value == null) {
			return super.set(Collections.emptyMap());
		} else {
			return super.set(new HashMap<>(value));
		}
	}

	@Override
	public Map<T1, T2> get() {
		return Collections.unmodifiableMap(super.get());
	}
}
