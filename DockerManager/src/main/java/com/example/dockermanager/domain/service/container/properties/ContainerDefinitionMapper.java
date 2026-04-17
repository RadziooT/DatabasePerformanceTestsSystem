package com.example.dockermanager.domain.service.container.properties;

import com.example.dockermanager.domain.model.container.ContainerDefinition;
import com.example.dockermanager.domain.model.container.ContainerType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ContainerDefinitionMapper {

    private final ContainerDefinitionPlaceholderResolver placeholderResolver;

    public Map<ContainerType, ContainerDefinition> mapFromConfigProperties(
            DockerContainersConfigProperties configProps) {
        Map<ContainerType, ContainerDefinition> definitions = new EnumMap<>(ContainerType.class);

        for (ContainerType type : ContainerType.values()) {
            ContainerDefinition dto = configProps.getContainers().get(type);
            validate(type, dto);
            definitions.put(type, placeholderResolver.resolve(dto));
        }

        return definitions;
    }

    private void validate(ContainerType type, ContainerDefinition dto) {
        if (Objects.isNull(dto)) {
            throw new IllegalStateException(
                    "docker.containers." + type.name() + " is required - missing in configuration"
            );
        }
        if (isBlank(dto.getContainerName())) {
            throw new IllegalStateException("docker.containers." + type.name() + ".containerName is required");
        }
        if (isBlank(dto.getImage())) {
            throw new IllegalStateException("docker.containers." + type.name() + ".image is required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
