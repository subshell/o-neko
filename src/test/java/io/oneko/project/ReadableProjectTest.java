package io.oneko.project;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.oneko.automations.LifetimeBehaviour;
import io.oneko.deployable.DeploymentBehaviour;
import io.oneko.kubernetes.deployments.DesiredState;

class ReadableProjectTest {

    @Test
    void testWritable() {
        ReadableProjectVersion version = new ReadableProjectVersion(UUID.randomUUID(),
                "7",
                DeploymentBehaviour.manually,
                Collections.emptyMap(),
                "sample content digest",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                false,
                LifetimeBehaviour.infinite(),
                null,
                DesiredState.Deployed,
                Instant.now());
        //prepare a readable
        ReadableProject underTest = ReadableProject.builder()
                .id(UUID.randomUUID())
                .dockerRegistryId(UUID.randomUUID())
                .name("test")
                .imageName("subshell/test")
                .versions(Collections.singletonList(version))
                .build();

        WritableProject writable = underTest.writable();
        assertThat(writable.getName()).isEqualTo(underTest.getName());
        assertThat(writable.getVersions()).hasSize(1);

        //modifying the writable should not affect the readable
        writable.setName("New Name");
        writable.createVersion("11");

        assertThat(underTest.getName()).isEqualTo("test");
        assertThat(underTest.getVersions()).hasSize(1);
    }

}
