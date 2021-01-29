package io.oneko.domain;

import java.util.UUID;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Base class for model objects where instance can be identified by their ID.
 */
public abstract class Identifiable {

	public abstract UUID getId();

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (o.getClass() != this.getClass()) {
			return false;
		}
		return this.getId().equals(((Identifiable) o).getId());
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(739, 599).append(this.getId()).toHashCode();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "_" + this.getId();
	}
}
