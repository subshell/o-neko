package io.oneko.event;

import io.oneko.domain.DescribingEntityChange;

public class EntityChangedEvent extends Event {

	private final DescribingEntityChange description;

	protected EntityChangedEvent(EventTrigger trigger, DescribingEntityChange description) {
		super(trigger);
		this.description = description;
	}

	public DescribingEntityChange describeEntityChange() {
		return this.description;
	}

	@Override
	public String humanReadable() {
		DescribingEntityChange description = describeEntityChange();
		return description.getEntityType() + " with name " + description.getName() + " has been " + description.getChangeType().toString().toLowerCase() + ".";
	}

}
