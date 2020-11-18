package io.oneko.projectmesh;

import io.oneko.docker.ReadableDockerRegistry;
import io.oneko.docker.WritableDockerRegistry;
import io.oneko.project.ProjectVersion;
import io.oneko.project.ReadableProject;
import io.oneko.project.WritableProject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MeshComponentTest {

	@Test
	void testChangeVersion() {
		ReadableDockerRegistry reg = new WritableDockerRegistry().readable();
		WritableProject p = new WritableProject(reg.getId());
		final ProjectVersion v1 = p.createVersion("v1");
		final ProjectVersion v2 = p.createVersion("v2");
		final ReadableProject readable = p.readable();

		WritableProjectMesh mesh = new WritableProjectMesh();
		WritableMeshComponent c = new WritableMeshComponent(mesh, readable.getId(), readable.getVersionByName("v1").get().getId());

		assertThat(c.getProjectVersionId(), is(v1.getId()));

		c.setProjectVersion(readable.getVersionByName("v2").get().getId());
		assertThat(c.getProjectVersionId(), is(v2.getId()));
	}

}
