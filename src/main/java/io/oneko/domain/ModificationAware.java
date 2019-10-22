package io.oneko.domain;

import java.util.Set;

public interface ModificationAware {

	/**
	 * Marks this entity as modified regardless of whether any property has been changed.
	 */
	void touch();

	/**
	 * Whether the entity has been modified since it has been instantiated.
	 */
	boolean isDirty();

	/**
	 * Provides the names of properties that have been modified.
	 */
	Set<String> getDirtyProperties();
}
