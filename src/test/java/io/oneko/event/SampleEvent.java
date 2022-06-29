package io.oneko.event;

public class SampleEvent extends Event {

	private final String name;

	protected SampleEvent(String name) {
		this.name = name;
	}

	protected SampleEvent(Void nothing) {
		this.name = "from the void";
	}

	@Override
	public String title() {
		return "SampleEvent " + this.name;
	}
}
