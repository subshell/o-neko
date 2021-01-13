package io.oneko.event;

import io.oneko.activity.ActivityPriority;
import io.oneko.domain.Identifiable;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

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
