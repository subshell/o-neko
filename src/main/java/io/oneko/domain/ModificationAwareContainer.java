package io.oneko.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ModificationAwareContainer extends ModificationAwareIdentifiable {

	private transient boolean dirty;
	private transient Set<String> dirtyProperties = new HashSet<>();

	@Override
	public void touch() {
		this.dirty = true;
	}

	@Override
	public boolean isDirty() {
		return dirty || !this.getDirtyProperties().isEmpty();
	}

	protected void touchProperty(String propertyName) {
		this.dirtyProperties.add(propertyName);
	}

	protected void resetProperty(String propertyName) {
		this.dirtyProperties.remove(propertyName);
	}

	@Override
	public Set<String> getDirtyProperties() {
		return Collections.unmodifiableSet(dirtyProperties);
	}

	@Override
	public UUID getId() {
		//TODO...remove this here
		return null;
	}
}
