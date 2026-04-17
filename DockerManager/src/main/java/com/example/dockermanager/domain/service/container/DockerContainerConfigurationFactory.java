package com.example.dockermanager.domain.service.container;

import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.service.container.configurators.ContainerConfiguration;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class DockerContainerConfigurationFactory {

    private final Map<ContainerType, ContainerConfiguration> configurationsByType;

    public DockerContainerConfigurationFactory(List<ContainerConfiguration> configurations) {
        this.configurationsByType = new EnumMap<>(ContainerType.class);
        for (ContainerConfiguration configuration : configurations) {
            ContainerConfiguration previous = configurationsByType.put(configuration.getContainerType(), configuration);
            if (previous != null) {
                throw new IllegalStateException("Duplicate container configuration for type: " + configuration.getContainerType());
            }
        }
    }

    public ContainerConfiguration forType(ContainerType type) {
        return getOrThrow(type);
    }

    private ContainerConfiguration getOrThrow(ContainerType type) {
        ContainerConfiguration config = configurationsByType.get(type);
        if (config == null) {
            throw new IllegalArgumentException("No container configuration registered for type: " + type);
        }
        return config;
    }
}
