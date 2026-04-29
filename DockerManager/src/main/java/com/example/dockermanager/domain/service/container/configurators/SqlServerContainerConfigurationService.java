package com.example.dockermanager.domain.service.container.configurators;

import com.example.dockermanager.domain.model.container.ContainerDefinition;
import com.example.dockermanager.domain.model.container.ContainerType;
import com.example.dockermanager.domain.model.container.configuration.RuntimeConfigurationOverrideInput;
import com.example.dockermanager.domain.service.container.DockerNetworkService;
import com.github.dockerjava.api.command.CreateContainerCmd;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class SqlServerContainerConfigurationService extends DatabaseContainerConfiguration {

    public SqlServerContainerConfigurationService(DockerNetworkService dockerNetworkService) {
        super(dockerNetworkService);
    }

    @Override
    protected Optional<String> volumeTargetPath() {
        return Optional.of("/var/opt/mssql");
    }

    @Override
    protected void applyCustomContainerConfig(CreateContainerCmd createContainerCmd,
                                              ContainerDefinition definition,
                                              RuntimeConfigurationOverrideInput runtimeConfiguration) {
        createContainerCmd.withHealthcheck(buildHealthCheck("/opt/mssql-tools18/bin/sqlcmd -S localhost -C -U sa -P ${MSSQL_SA_PASSWORD:-$SA_PASSWORD} -Q 'SELECT 1' || exit 1"));
    }

    @Override
    protected Map<String, String> runtimeEnvironmentOverrides(RuntimeConfigurationOverrideInput runtimeConfiguration) {
        Map<String, String> overrides = new java.util.LinkedHashMap<>();
        overrides.put("MSSQL_MEMORY_LIMIT_MB", "3072");
        overrides.put("MSSQL_MAX_CONNECTIONS", "200");
        return overrides;
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.SQLSERVER;
    }
}
