package com.example.dockermanager.domain.service.container.properties;

import com.example.dockermanager.domain.model.container.ContainerDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ContainerDefinitionPlaceholderResolver {

    private final Environment environment;

    public ContainerDefinition resolve(ContainerDefinition source) {
        ContainerDefinition resolved = new ContainerDefinition();
        resolved.setContainerName(resolveValue(source.getContainerName()));
        resolved.setImage(resolveValue(source.getImage()));
        resolved.setPorts(resolveList(source.getPorts()));
        resolved.setVolumes(resolveList(source.getVolumes()));
        resolved.setEnvironment(resolveMap(source.getEnvironment()));
        resolved.setLabels(resolveMap(source.getLabels()));
        return resolved;
    }

    private List<String> resolveList(List<String> values) {
        return Optional.ofNullable(values)
                .orElse(List.of())
                .stream()
                .map(this::resolveValue)
                .toList();
    }

    private Map<String, String> resolveMap(Map<String, String> values) {
        return Optional.ofNullable(values)
                .orElse(Map.of())
                .entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        entry -> resolveValue(entry.getKey()),
                        entry -> resolveValue(entry.getValue())
                ));
    }

    private String resolveValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return environment.resolveRequiredPlaceholders(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unresolved placeholder in container definition value: " + value, ex);
        }
    }
}
