package io.oneko.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Objects;

public class ModificationAwareProperty<T> implements Serializable {

	private final ModificationAwareIdentifiable entity;
	private final String propertyName;
	private T initialValue;
	private T currentValue;

	public ModificationAwareProperty(ModificationAwareIdentifiable entity, String propertyName) {
		this.entity = entity;
		this.propertyName = propertyName;
	}

	public void init(T value) {
		this.initialValue = value;
		this.currentValue = value;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public T get() {
		return currentValue;
	}

	/**
	 * Sets the current value to the given one.
	 *
	 * @param value
	 * @return true if the value has actually been changed.
	 */
	public boolean set(T value) {
		if (Objects.equals(value, this.currentValue)) {
			return false;
		}
		this.currentValue = value;
		if (isDirty()) {
			this.entity.touchProperty(this.propertyName);
		} else {
			this.entity.resetProperty(this.propertyName);
		}
		return true;
	}

	public boolean isDirty() {
		return !Objects.equals(this.initialValue, this.currentValue);
	}

	@Override
	public String toString() {
		return new StringBuilder()
				.append(currentValue)
				.append(" (")
				.append(this.isDirty() ? "dirty" : "not dirty")
				.append(")").toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		ModificationAwareProperty rhs = (ModificationAwareProperty) obj;
		return new EqualsBuilder()
				.append(entity, rhs.entity)
				.append(propertyName, rhs.propertyName)
				.append(initialValue, rhs.initialValue)
				.append(currentValue, rhs.currentValue)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(193, 43)
				.append(entity)
				.append(propertyName)
				.append(initialValue)
				.append(currentValue)
				.toHashCode();
	}
}
