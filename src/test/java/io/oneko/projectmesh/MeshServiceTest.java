package io.oneko.projectmesh;

import io.oneko.InMemoryTestBench;
import io.oneko.docker.ReadableDockerRegistry;
import io.oneko.docker.WritableDockerRegistry;
import io.oneko.project.*;
import io.oneko.templates.WritableConfigurationTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class MeshServiceTest {

    private InMemoryTestBench testBench;
    private MeshService uut;

    @BeforeEach
    void setup() {
        testBench = InMemoryTestBench.empty();
        uut = testBench.meshService;
    }

    @Test
    void testTemplateComposition() {
        ReadableDockerRegistry reg = new WritableDockerRegistry().readable();
        WritableProject p = new WritableProject(reg.getId());
        p.setDefaultConfigurationTemplates(Arrays.asList(
                new WritableConfigurationTemplate(UUID.randomUUID(), "content1 ${a}", "name1", ""),
                new WritableConfigurationTemplate(UUID.randomUUID(), "content2 ${b}", "name2", ""),
                new WritableConfigurationTemplate(UUID.randomUUID(), "content3 ${c}", "name3", ""),
                new WritableConfigurationTemplate(UUID.randomUUID(), "content4 ${d}", "name4", "")));
        List<WritableTemplateVariable> defaultTemplateVariables = new ArrayList<>();
        defaultTemplateVariables.add(new WritableTemplateVariable("a", "a", Collections.singletonList("a1"), true, "a1", false));
        defaultTemplateVariables.add(new WritableTemplateVariable("b", "b", Collections.singletonList("b1"), true, "b1", false));
        defaultTemplateVariables.add(new WritableTemplateVariable("c", "b", Collections.singletonList("c1"), true, "c1", false));
        defaultTemplateVariables.add(new WritableTemplateVariable("d", "b", Collections.singletonList("d1"), true, "d1", false));
        p.setTemplateVariables(defaultTemplateVariables);

        final WritableProjectVersion v = p.createVersion("v1");
        v.setConfigurationTemplates(Arrays.asList(
                new WritableConfigurationTemplate(UUID.randomUUID(), "content3 ${c} from version", "name3", ""),
                new WritableConfigurationTemplate(UUID.randomUUID(), "content4 ${d} from version", "name4", ""),
                new WritableConfigurationTemplate(UUID.randomUUID(), "content5 ${e} from version", "name5", "")));

        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("b", "b2");
        templateVariables.put("e", "e2");
        v.setTemplateVariables(templateVariables);

        final ReadableProject readable = testBench.projectRepository.add(p);;

        WritableProjectMesh mesh = new WritableProjectMesh();
        WritableMeshComponent c = new WritableMeshComponent(mesh, readable.getId(), readable.getVersionByName(v.getName()).get().getId());
        c.setConfigurationTemplates(Arrays.asList(
                new WritableConfigurationTemplate(UUID.randomUUID(), "content2 ${b} from mesh", "name2", ""),
                new WritableConfigurationTemplate(UUID.randomUUID(), "content4 ${d} from mesh", "name4", ""),
                new WritableConfigurationTemplate(UUID.randomUUID(), "content6 ${f} from mesh", "name6", "")));

        Map<String, String> meshTemplateVariables = new HashMap<>();
        meshTemplateVariables.put("c", "c3");
        meshTemplateVariables.put("f", "f3");
        c.setTemplateVariables(meshTemplateVariables);

        final List<WritableConfigurationTemplate> calculatedConfigurationTemplates = uut.getCalculatedConfigurationTemplates(c);
        assertThat(calculatedConfigurationTemplates, hasSize(6));
        assertThat(calculatedConfigurationTemplates.get(0).getName(), is("name1"));
        assertThat(calculatedConfigurationTemplates.get(0).getContent(), is("content1 a1"));

        assertThat(calculatedConfigurationTemplates.get(1).getName(), is("name2"));
        assertThat(calculatedConfigurationTemplates.get(1).getContent(), is("content2 b2 from mesh"));

        assertThat(calculatedConfigurationTemplates.get(2).getName(), is("name3"));
        assertThat(calculatedConfigurationTemplates.get(2).getContent(), is("content3 c3 from version"));

        assertThat(calculatedConfigurationTemplates.get(3).getName(), is("name4"));
        assertThat(calculatedConfigurationTemplates.get(3).getContent(), is("content4 d1 from mesh"));

        assertThat(calculatedConfigurationTemplates.get(4).getName(), is("name5"));
        assertThat(calculatedConfigurationTemplates.get(4).getContent(), is("content5 e2 from version"));

        assertThat(calculatedConfigurationTemplates.get(5).getName(), is("name6"));
        assertThat(calculatedConfigurationTemplates.get(5).getContent(), is("content6 f3 from mesh"));
    }
}
