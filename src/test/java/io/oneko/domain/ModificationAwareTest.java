package io.oneko.domain;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;


class ModificationAwareTest {

	@Test
	void testDontBeDirtyOnInit() {
		SampleModificationAware uut = new SampleModificationAware(UUID.randomUUID(), "test");

		assertThat(uut.isDirty(), is(false));
		assertThat(uut.uuid.isDirty(), is(false));
		assertThat(uut.name.isDirty(), is(false));
	}

	@Test
	void testExplicitlySetDirty() {
		SampleModificationAware uut = new SampleModificationAware(UUID.randomUUID(), "test");

		uut.touch();

		assertThat(uut.isDirty(), is(true));
		assertThat(uut.uuid.isDirty(), is(false));
		assertThat(uut.name.isDirty(), is(false));
	}

	@Test
	void testSetPropertyDirty() {
		SampleModificationAware uut = new SampleModificationAware(UUID.randomUUID(), "test");

		uut.setName("test2");
		assertThat(uut.isDirty(), is(true));
		assertThat(uut.uuid.isDirty(), is(false));
		assertThat(uut.name.isDirty(), is(true));

		uut.setName("test");
		assertThat(uut.isDirty(), is(false));
	}

	private class SampleModificationAware extends ModificationAwareIdentifiable {
		private final ModificationAwareProperty<UUID> uuid = new ModificationAwareProperty<>(this, "uuid");
		private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");

		/**
		 * Creates a completely new DockerRegistry
		 */
		public SampleModificationAware() {
			this.uuid.set(UUID.randomUUID());
		}

		public SampleModificationAware(UUID uuid, String name) {
			this.uuid.init(uuid);
			this.name.init(name);
		}

		@Override
		public UUID getId() {
			return this.uuid.get();
		}

		public String getName() {
			return name.get();
		}

		public void setName(String name) {
			this.name.set(name);
		}
	}
}
