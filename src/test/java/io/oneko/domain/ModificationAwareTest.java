package io.oneko.domain;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ModificationAwareTest {

	@Test
	void testDontBeDirtyOnInit() {
		SampleModificationAware uut = new SampleModificationAware(UUID.randomUUID(), "test");

		assertThat(uut.isDirty()).isFalse();
		assertThat(uut.id.isDirty()).isFalse();
		assertThat(uut.name.isDirty()).isFalse();
	}

	@Test
	void testExplicitlySetDirty() {
		SampleModificationAware uut = new SampleModificationAware(UUID.randomUUID(), "test");

		uut.touch();

		assertThat(uut.isDirty()).isTrue();
		assertThat(uut.id.isDirty()).isFalse();
		assertThat(uut.name.isDirty()).isFalse();
	}

	@Test
	void testSetPropertyDirty() {
		SampleModificationAware uut = new SampleModificationAware(UUID.randomUUID(), "test");

		uut.setName("test2");
		assertThat(uut.isDirty()).isTrue();
		assertThat(uut.id.isDirty()).isFalse();
		assertThat(uut.name.isDirty()).isTrue();

		uut.setName("test");
		assertThat(uut.isDirty()).isFalse();
	}

	private class SampleModificationAware extends ModificationAwareIdentifiable {
		private final ModificationAwareProperty<UUID> id = new ModificationAwareProperty<>(this, "id");
		private final ModificationAwareProperty<String> name = new ModificationAwareProperty<>(this, "name");

		/**
		 * Creates a completely new DockerRegistry
		 */
		public SampleModificationAware() {
			this.id.set(UUID.randomUUID());
		}

		public SampleModificationAware(UUID id, String name) {
			this.id.init(id);
			this.name.init(name);
		}

		@Override
		public UUID getId() {
			return this.id.get();
		}

		public String getName() {
			return name.get();
		}

		public void setName(String name) {
			this.name.set(name);
		}
	}
}
