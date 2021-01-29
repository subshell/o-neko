package io.oneko.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Base implementation for {@link ModificationAware}. <br/>
 * Extends {@link Identifiable} as well - not because it is really needed, but O-Neko-Entities tend to be Identifiable
 * and Java does not support multi inheritance so {@link ModificationAwareIdentifiable} has to be both.
 */
public abstract class ModificationAwareIdentifiable extends Identifiable implements ModificationAware {

	private transient boolean dirty;
	private final transient Set<String> dirtyProperties = new HashSet<>();

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
}
