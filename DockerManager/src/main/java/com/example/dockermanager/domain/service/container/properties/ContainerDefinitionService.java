package com.example.dockermanager.domain.service.container.properties;

import com.example.dockermanager.domain.model.container.ContainerDefinition;
import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.container.configuration.SharedDatabaseCredentialsProperties;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for retrieving and resolving container definitions.
 * Separates configuration loading (mapper) from placeholder resolution
 */
@Service
@RequiredArgsConstructor
public class ContainerDefinitionService {

    private final DockerContainersConfigProperties configProperties;
    private final ContainerDefinitionMapper configMapper;

    private Map<ContainerType, ContainerDefinition> cachedDefinitions;
    @Getter
    private SharedDatabaseCredentialsProperties sharedDbCredentials;

    @PostConstruct
    public void init() {
        this.cachedDefinitions = configMapper.mapFromConfigProperties(configProperties);
        this.sharedDbCredentials = configProperties.getSharedDb();
    }

    public Optional<ContainerDefinition> getDefinition(ContainerType containerType) {
        return Optional.ofNullable(cachedDefinitions.get(containerType));
    }

    public List<String> getContainerTypesNames() {
        return cachedDefinitions.values().stream()
                .map(ContainerDefinition::getContainerName)
                .toList();
    }
}
