package io.oneko.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModificationAwareListProperty<T> extends ModificationAwareProperty<List<T>> {

	public ModificationAwareListProperty(ModificationAwareIdentifiable entity, String propertyName) {
		super(entity, propertyName);
		super.init(Collections.emptyList());
	}

	@Override
	public void init(List<T> value) {
		if (value == null) {
			super.init(Collections.emptyList());
		} else {
			super.init(new ArrayList<>(value));
		}
	}

	@Override
	public boolean set(List<T> value) {
		if (value == null) {
			return super.set(Collections.emptyList());
		} else {
			return super.set(new ArrayList<>(value));
		}
	}

	@Override
	public List<T> get() {
		return Collections.unmodifiableList(super.get());
	}
}
