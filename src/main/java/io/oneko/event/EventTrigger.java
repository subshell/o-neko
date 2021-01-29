package io.oneko.event;

import java.util.UUID;

import io.oneko.activity.ActivityPriority;
import io.oneko.domain.Identifiable;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public abstract class EventTrigger extends Identifiable {

	private final UUID id;

	protected EventTrigger() {
		this.id = UUID.randomUUID();
	}

	public abstract String humanReadable();

	public abstract ActivityPriority priority();

	public abstract String getType();
}
