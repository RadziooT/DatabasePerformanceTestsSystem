package com.example.dockermanager.domain.service.container.configurators;

import com.example.dockermanager.domain.model.container.ContainerDefinition;
import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.container.configuration.DatabaseConnectionConfig;
import com.example.dockermanager.domain.model.container.configuration.RuntimeConfigurationOverrideInput;
import com.example.dockermanager.domain.service.container.DockerNetworkService;
import com.example.dockermanager.domain.service.container.properties.ContainerDefinitionService;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.RestartPolicy;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MockAppContainerConfiguration extends ContainerConfiguration {

    private final ContainerDefinitionService containerDefinitionService;

    public MockAppContainerConfiguration(DockerNetworkService dockerNetworkService, ContainerDefinitionService containerDefinitionService) {
        super(dockerNetworkService);
        this.containerDefinitionService = containerDefinitionService;
    }

    @Override
    protected Map<String, String> runtimeEnvironmentOverrides(RuntimeConfigurationOverrideInput runtimeConfiguration) {
        if (runtimeConfiguration.getDatabaseType() == null) {
            return Map.of();
        }

        ContainerType dbType = runtimeConfiguration.getDatabaseType().toContainerType();
        var databaseProperties = resolveDatabaseConnection(dbType);

        Map<String, String> overrides = new LinkedHashMap<>();
        overrides.put("SPRING_PROFILES_ACTIVE", dbType.name().toLowerCase());
        overrides.put("DB_HOST", databaseProperties.host());
        overrides.put("DB_PORT", String.valueOf(databaseProperties.port()));
        overrides.put("DB_NAME", databaseProperties.databaseName());
        overrides.put("DB_USERNAME", databaseProperties.user());
        overrides.put("DB_PASSWORD", databaseProperties.password());

        return overrides;
    }

    @Override
    protected void applyCustomHostConfig(HostConfig hostConfig) {
        hostConfig
                .withMemory(536_870_912L)
                .withRestartPolicy(RestartPolicy.onFailureRestart(3));
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.MOCK_APP;
    }

    private DatabaseConnectionConfig resolveDatabaseConnection(ContainerType containerType) {
        ContainerDefinition definition = containerDefinitionService.getDefinition(containerType)
                .orElseThrow(() -> new IllegalStateException("Missing container definition for " + containerType));

        var credentials = containerDefinitionService.getSharedDbCredentials();
        return new DatabaseConnectionConfig(
                definition.getContainerName(),
                resolveContainerPort(definition, containerType),
                credentials.getName(),
                credentials.getUsername(),
                credentials.getPassword()
        );
    }

    private int resolveContainerPort(ContainerDefinition definition, ContainerType containerType) {
        List<String> ports = Optional.ofNullable(definition.getPorts()).orElse(List.of());
        if (ports.isEmpty()) {
            throw new IllegalStateException("Missing port mapping for " + containerType);
        }

        String[] parts = ports.getFirst().split(":");
        if (parts.length != 2) {
            throw new IllegalStateException("Invalid port mapping for " + containerType + ": " + ports.getFirst());
        }

        try {
            return Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Invalid container port for " + containerType + ": " + ports.getFirst(), ex);
        }
    }
}
