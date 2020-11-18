package io.oneko.projectmesh;

import io.oneko.project.*;
import io.oneko.templates.ConfigurationTemplate;
import io.oneko.templates.ConfigurationTemplates;
import io.oneko.templates.WritableConfigurationTemplate;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MeshService {

    private final ProjectRepository projectRepository;

    public MeshService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public ReadableProjectVersion getVersionOfComponent(MeshComponent component) {
        return projectRepository.getById(component.getProjectId())
                .flatMap(p -> p.getVersionById(component.getProjectVersionId()))
                .orElse(null);
    }

    public Map<String, String> calculateEffectiveTemplateVariables(MeshComponent component) {
        final Optional<ReadableProject> project = projectRepository.getById(component.getProjectId());
        final Optional<ReadableProjectVersion> version = project.flatMap(p -> p.getVersionById(component.getProjectVersionId()));
        return calculateEffectiveTemplateVariables(component, project.orElse(null), version.orElse(null));
    }

    private Map<String, String> calculateEffectiveTemplateVariables(MeshComponent component, ReadableProject p, ReadableProjectVersion v) {
        Map<String, String> mergedTemplateVariables = new HashMap<>();
        if (v!= null) {
            mergedTemplateVariables.putAll(v.getImplicitTemplateVariables());
        }
        if (p != null) {
            for (TemplateVariable tv : p.getTemplateVariables()) {
                mergedTemplateVariables.put(tv.getName(), tv.getDefaultValue());
            }
        }
        if (v != null) {
            mergedTemplateVariables.putAll(v.getTemplateVariables());
        }
        mergedTemplateVariables.putAll(component.getImplicitTemplateVariables());
        mergedTemplateVariables.putAll(component.getTemplateVariables());
        return mergedTemplateVariables;
    }

    /**
     * Provides all effective templates to use on this component. This is either derived from the project's configuration
     * template, a modified version template or a modified template straight from this component with the effective
     * template variables filled in.
     */
    public List<WritableConfigurationTemplate> getCalculatedConfigurationTemplates(MeshComponent component) {
        final Optional<ReadableProject> project = projectRepository.getById(component.getProjectId());
        final Optional<ReadableProjectVersion> version = project.flatMap(p -> p.getVersionById(component.getProjectVersionId()));
        StringSubstitutor sub = new StringSubstitutor(calculateEffectiveTemplateVariables(component, project.orElse(null), version.orElse(null)));
        List<Collection<? extends ConfigurationTemplate>> templateSets = new ArrayList<>();
        project.ifPresent(p -> templateSets.add(p.getDefaultConfigurationTemplates()));
        version.ifPresent(v -> templateSets.add(v.getConfigurationTemplates()));
        templateSets.add(component.getConfigurationTemplates());
        return ConfigurationTemplates.unifyTemplateSets(templateSets)
                .stream()
                .map(WritableConfigurationTemplate::clone)
                .peek(template -> template.setContent(sub.replace(template.getContent())))
                .collect(Collectors.toList());
    }
}
