package com.example.dockermanager.domain.service.container.configurators;

import com.example.dockermanager.domain.model.container.ContainerDefinition;
import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.container.configuration.DataGenerationContainerParams;
import com.example.dockermanager.domain.model.container.configuration.DatabaseConnectionConfig;
import com.example.dockermanager.domain.model.container.configuration.RuntimeConfigurationOverrideInput;
import com.example.dockermanager.domain.service.container.DockerNetworkService;
import com.example.dockermanager.domain.service.container.properties.ContainerDefinitionService;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.RestartPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DataGeneratorContainerConfigurationService extends ContainerConfiguration {

    private static final long DATA_GENERATOR_MEMORY_LIMIT_BYTES = 2_147_483_648L; // 2GB
    private static final long DATA_GENERATOR_CPU_COUNT = 2L;

    private final ContainerDefinitionService containerDefinitionService;
    @Value("${data-generation.callback-url}")
    private String callbackUrl;
    @Value("${data-generation.callback-token:docker-manager-dev-token}")
    private String callbackToken;

    public DataGeneratorContainerConfigurationService(DockerNetworkService dockerNetworkService, ContainerDefinitionService containerDefinitionService) {
        super(dockerNetworkService);
        this.containerDefinitionService = containerDefinitionService;
    }

    @Override
    protected Map<String, String> runtimeEnvironmentOverrides(RuntimeConfigurationOverrideInput runtimeConfiguration) {
        DataGenerationContainerParams request = runtimeConfiguration.getDataGenerationContainerParams();
        if (request == null) {
            return Map.of();
        }

        var databaseProperties = resolveDatabaseConnection(request.databaseType());

        Map<String, String> env = new LinkedHashMap<>();
        env.put("GENERATOR_JOB_ID", request.jobId());
        env.put("GENERATOR_DB_TYPE", request.databaseType().name().toUpperCase());
        env.put("GENERATOR_VOLUME_TYPE", request.volumeType().name().toUpperCase());
        env.put("GENERATOR_DB_NETWORK_NAME", request.databaseType().getContainerName());
        env.put("GENERATOR_DB_PORT", String.valueOf(databaseProperties.port()));
        env.put("GENERATOR_DB_NAME", databaseProperties.databaseName());
        env.put("GENERATOR_DB_USER", databaseProperties.user());
        env.put("GENERATOR_DB_PASSWORD", databaseProperties.password());
        env.put("GENERATOR_CALLBACK_URL", callbackUrl);
        env.put("GENERATOR_CALLBACK_TOKEN", callbackToken);
        env.put("JAVA_OPTS", "-Xms2g -Xmx2g");

        return env;
    }

    @Override
    protected void applyCustomHostConfig(HostConfig hostConfig) {
        hostConfig
                .withAutoRemove(true)
                .withMemory(DATA_GENERATOR_MEMORY_LIMIT_BYTES)
                .withMemorySwap(DATA_GENERATOR_MEMORY_LIMIT_BYTES)
                .withCpuCount(DATA_GENERATOR_CPU_COUNT)
                .withCpuPeriod(100_000L)
                .withCpuQuota(200_000L)
                .withRestartPolicy(RestartPolicy.noRestart());
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.DATA_GENERATOR;
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
